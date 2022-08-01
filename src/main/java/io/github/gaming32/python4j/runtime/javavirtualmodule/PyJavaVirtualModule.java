package io.github.gaming32.python4j.runtime.javavirtualmodule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.runtime.PyArguments;
import io.github.gaming32.python4j.runtime.PyModule;
import io.github.gaming32.python4j.runtime.PySimpleFunctionObject;
import io.github.gaming32.python4j.util.AutoComputingAbsentMap;

public final class PyJavaVirtualModule implements PyModule {
    private static class PropertyWrapper {
        static final MethodType GETTER_INTERFACE_TYPE = MethodType.methodType(Supplier.class);
        static final MethodType GETTER_GENERIC_TYPE = MethodType.methodType(Object.class);
        static final MethodType GETTER_ACTUAL_TYPE = MethodType.methodType(PyObject.class);
        static final MethodType SETTER_INTERFACE_TYPE = MethodType.methodType(Consumer.class);
        static final MethodType SETTER_GENERIC_TYPE = MethodType.methodType(void.class, Object.class);
        static final MethodType SETTER_ACTUAL_TYPE = MethodType.methodType(void.class, PyObject.class);

        Supplier<PyObject> getter;
        Consumer<PyObject> setter;
    }

    private static final MethodType FUNCTION_INTERFACE_TYPE = MethodType.methodType(Function.class);
    private static final MethodType FUNCTION_GENERIC_TYPE = MethodType.methodType(Object.class, Object.class);
    private static final MethodType FUNCTION_ACTUAL_TYPE = MethodType.methodType(PyObject.class, PyArguments.class);

    private static Map<String, PyModule> virtualModules = null;

    private final String name;
    private final JavaVirtualModule internalModule;
    private final Map<String, Object> contents;
    private final String[] dir;
    private final String[] all;

    private PyJavaVirtualModule(JavaVirtualModule module) throws IllegalAccessException {
        name = module.getName();
        internalModule = module;
        contents = new LinkedHashMap<>();
        computeContents(module.getClass(), module.lookup);
        dir = contents.keySet().toArray(new String[contents.size()]);
        String[] all = module.getAll();
        if (all == null) {
            all = new String[dir.length];
            int i, j;
            for (i = 0, j = 0; j < dir.length; j++) {
                if (dir[j].startsWith("_")) continue;
                all[i++] = dir[j];
            }
            this.all = i == j ? all : Arrays.copyOf(all, i);
        } else {
            this.all = all;
        }
    }

    public static Map<String, PyModule> getVirtualModules() throws IOException {
        if (virtualModules != null) return virtualModules;
        final Map<String, Object> moduleLocations = new HashMap<>();
        try {
            for (final URL resource : (Iterable<URL>)() -> {
                try {
                    return PyJavaVirtualModule.class.getClassLoader().getResources("META-INF/python-modules").asIterator();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final int hashIndex = line.indexOf('#');
                        if (hashIndex >= 0) line = line.substring(0, hashIndex);
                        line = line.trim();
                        if (line.isEmpty()) continue;
                        final int equalsIndex = line.indexOf('=');
                        if (equalsIndex == -1) {
                            // Use the line for the key, I guess. The user didn't specify a proper key/value pair.
                            moduleLocations.put(line, new RuntimeException("Line in python-modules missing module declaration: " + line));
                            continue;
                        }
                        final String moduleName = line.substring(0, equalsIndex);
                        final int slashIndex = line.indexOf('/', equalsIndex);
                        if (slashIndex == -1) {
                            moduleLocations.put(moduleName, new RuntimeException("Line in python-modules missing module source type: " + line));
                            continue;
                        }
                        final String sourceType = line.substring(equalsIndex + 1, slashIndex);
                        if (!sourceType.equals("java")) {
                            moduleLocations.put(moduleName, new RuntimeException("Line in python-modules has unsupported source type: " + line));
                            continue;
                        }
                        moduleLocations.put(moduleName, line.substring(slashIndex + 1));
                    }
                }
            }
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        return new AutoComputingAbsentMap<>(new HashMap<>(), key -> {
            final Object maybeLocation = moduleLocations.get(key);
            if (maybeLocation == null) {
                return null;
            }
            if (maybeLocation instanceof RuntimeException) {
                final RuntimeException e = (RuntimeException)maybeLocation;
                e.setStackTrace(new Throwable().getStackTrace());
                throw e;
            }
            try {
                final Class<?> clazz = Class.forName((String)maybeLocation);
                final Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
                return new PyJavaVirtualModule((JavaVirtualModule)constructor.newInstance(key));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static PyModule getVirtualModule(String module) throws ReflectiveOperationException, IOException {
        try {
            return getVirtualModules().get(module);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ReflectiveOperationException) {
                throw (ReflectiveOperationException)e.getCause();
            }
            if (e.getCause() instanceof IOException) {
                throw (IOException)e.getCause();
            }
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private void computeContents(Class<? extends JavaVirtualModule> clazz, MethodHandles.Lookup lookup) throws IllegalAccessException {
        for (final Field field : clazz.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            final ModuleConstant anno = field.getAnnotation(ModuleConstant.class);
            if (anno != null) {
                String name = anno.value();
                if (name.isEmpty()) name = field.getName();
                if (Modifier.isFinal(field.getModifiers())) {
                    contents.put(name, field.get(null));
                } else {
                    ((PropertyWrapper)contents.computeIfAbsent(name, key -> new PropertyWrapper()))
                        .getter = (Supplier<PyObject>)MethodHandleProxies
                            .asInterfaceInstance(Supplier.class, lookup.unreflectGetter(field));
                    if (!Modifier.isFinal(field.getModifiers())) {
                        ((PropertyWrapper)contents.computeIfAbsent(name, key -> new PropertyWrapper()))
                            .setter = (Consumer<PyObject>)MethodHandleProxies
                                .asInterfaceInstance(Consumer.class, lookup.unreflectSetter(field));
                    }
                }
            }
        }
        for (final Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            {
                final ModuleMethod methodAnno = method.getAnnotation(ModuleMethod.class);
                if (methodAnno != null) {
                    String name = methodAnno.value();
                    if (name.isEmpty()) name = method.getName();
                    try {
                        contents.put(name, new PySimpleFunctionObject(
                            (Function<PyArguments, PyObject>)LambdaMetafactory.metafactory(
                                lookup,
                                "apply",
                                FUNCTION_INTERFACE_TYPE,
                                FUNCTION_GENERIC_TYPE,
                                lookup.unreflect(method),
                                FUNCTION_ACTUAL_TYPE
                            ).getTarget().invokeExact()
                        ));
                    } catch (IllegalAccessException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new Error(t);
                    }
                }
            }
            {
                final ModuleProperty.Getter getterAnno = method.getAnnotation(ModuleProperty.Getter.class);
                if (getterAnno != null) {
                    String name = getterAnno.value();
                    if (name.isEmpty()) {
                        name = method.getName();
                        if (name.length() > 3 && name.startsWith("get")) {
                            final String origName = name;
                            name = Character.toString(Character.toLowerCase(name.charAt(3)));
                            if (origName.length() > 4) {
                                name += origName.substring(4);
                            }
                        }
                    }
                    try {
                        ((PropertyWrapper)contents.computeIfAbsent(name, key -> new PropertyWrapper()))
                            .getter = (Supplier<PyObject>)LambdaMetafactory.metafactory(
                                lookup,
                                "get",
                                PropertyWrapper.GETTER_INTERFACE_TYPE,
                                PropertyWrapper.GETTER_GENERIC_TYPE,
                                lookup.unreflect(method),
                                PropertyWrapper.GETTER_ACTUAL_TYPE
                            ).getTarget().invokeExact();
                    } catch (IllegalAccessException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new Error(t);
                    }
                }
            }
            {
                final ModuleProperty.Setter setterAnno = method.getAnnotation(ModuleProperty.Setter.class);
                if (setterAnno != null) {
                    String name = setterAnno.value();
                    if (name.isEmpty()) {
                        name = method.getName();
                        if (name.length() > 3 && name.startsWith("set")) {
                            final String origName = name;
                            name = Character.toString(Character.toLowerCase(name.charAt(3)));
                            if (origName.length() > 4) {
                                name += origName.substring(4);
                            }
                        }
                    }
                    try {
                        ((PropertyWrapper)contents.computeIfAbsent(name, key -> new PropertyWrapper()))
                            .setter = (Consumer<PyObject>)LambdaMetafactory.metafactory(
                                lookup,
                                "accept",
                                PropertyWrapper.SETTER_INTERFACE_TYPE,
                                PropertyWrapper.SETTER_GENERIC_TYPE,
                                lookup.unreflect(method),
                                PropertyWrapper.SETTER_ACTUAL_TYPE
                            ).getTarget().invokeExact();
                    } catch (IllegalAccessException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new Error(t);
                    }
                }
            }
        }
    }

    @Override
    public void init() {
        internalModule.init();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PyObject getattr(String name) {
        final Object value = contents.get(name);
        if (value instanceof PropertyWrapper) {
            return ((PropertyWrapper)value).getter.get();
        }
        if (value != null) {
            return (PyObject)value;
        }
        return internalModule.getattr(name);
    }

    @Override
    public boolean setattr(String name, PyObject value) {
        final Object attr = contents.get(name);
        if (attr instanceof PropertyWrapper) {
            ((PropertyWrapper)attr).setter.accept(value);
            return true;
        }
        return internalModule.setattr(name, value);
    }

    @Override
    public String[] dir() {
        return dir.clone();
    }

    @Override
    public String[] all() {
        return all.clone();
    }
}
