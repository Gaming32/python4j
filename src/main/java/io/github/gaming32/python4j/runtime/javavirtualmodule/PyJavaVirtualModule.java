package io.github.gaming32.python4j.runtime.javavirtualmodule;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.runtime.PyArguments;
import io.github.gaming32.python4j.runtime.PySimpleFunctionObject;
import io.github.gaming32.python4j.runtime.PyModule;

public final class PyJavaVirtualModule implements PyModule {
    private static class PropertyWrapper {
        static final MethodType GETTER_INTERFACE_TYPE = MethodType.methodType(Supplier.class);
        static final MethodType GETTER_GENERIC_TYPE = MethodType.methodType(Object.class);
        static final MethodType GETTER_ACTUAL_TYPE = MethodType.methodType(PyObject.class);
        static final MethodType SETTER_INTERFACE_TYPE = MethodType.methodType(Consumer.class);
        static final MethodType SETTER_GENERIC_TYPE = MethodType.methodType(void.class, Object.class);
        static final MethodType SETTER_ACTUAL_TYPE = MethodType.methodType(void.class, PyObject.class);
        static final MethodType DELETER_INTERFACE_TYPE = MethodType.methodType(Runnable.class);
        static final MethodType DELETER_GENERIC_TYPE = MethodType.methodType(void.class);

        Supplier<PyObject> getter;
        Consumer<PyObject> setter;
        Runnable deleter;
    }

    private static final MethodType FUNCTION_INTERFACE_TYPE = MethodType.methodType(Function.class);
    private static final MethodType FUNCTION_GENERIC_TYPE = MethodType.methodType(Object.class, Object.class);
    private static final MethodType FUNCTION_ACTUAL_TYPE = MethodType.methodType(PyObject.class, PyArguments.class);

    private static Map<String, PyModule> virtualModules = null;

    private final String name;
    private final Map<String, Object> contents;
    private final String[] dir;
    private final String[] all;

    private PyJavaVirtualModule(JavaVirtualModule module) throws IllegalAccessException {
        name = module.getName();
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

    public static Map<String, PyModule> getVirtualModules() throws IllegalAccessException {
        if (virtualModules != null) return virtualModules;
        try {
            return virtualModules = ServiceLoader.load(JavaVirtualModuleMarker.class)
                .stream()
                .map(p -> {
                    try {
                        return new PyJavaVirtualModule((JavaVirtualModule)p.get());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toMap(m -> m.getName(), m -> m));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IllegalAccessException) {
                throw (IllegalAccessException)e.getCause();
            }
            throw e;
        }
    }

    private void computeContents(Class<? extends JavaVirtualModuleMarker> clazz, MethodHandles.Lookup lookup) throws IllegalAccessException {
        for (final Method method : clazz.getMethods()) {
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
            {
                final ModuleProperty.Deleter deleterAnno = method.getAnnotation(ModuleProperty.Deleter.class);
                if (deleterAnno != null) {
                    String name = deleterAnno.value();
                    if (name.isEmpty()) {
                        name = method.getName();
                        if (name.length() > 6 && name.startsWith("delete")) {
                            final String origName = name;
                            name = Character.toString(Character.toLowerCase(name.charAt(6)));
                            if (origName.length() > 7) {
                                name += origName.substring(7);
                            }
                        }
                    }
                    try {
                        ((PropertyWrapper)contents.computeIfAbsent(name, key -> new PropertyWrapper()))
                            .deleter = (Runnable)LambdaMetafactory.metafactory(
                                lookup,
                                "run",
                                PropertyWrapper.DELETER_INTERFACE_TYPE,
                                PropertyWrapper.DELETER_GENERIC_TYPE,
                                lookup.unreflect(method),
                                PropertyWrapper.DELETER_GENERIC_TYPE
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
    public String getName() {
        return name;
    }

    @Override
    public PyObject getattr(String name) {
        final Object value = contents.get(name);
        if (value instanceof PropertyWrapper) {
            return ((PropertyWrapper)value).getter.get();
        }
        return (PyObject)value;
    }

    @Override
    public void setattr(String name, PyObject value) {
        final Object attr = contents.get(name);
        if (attr instanceof PropertyWrapper) {
            ((PropertyWrapper)attr).setter.accept(value);
            return;
        }
        throw new IllegalArgumentException("Cannot assign to function " + name);
    }

    @Override
    public void delattr(String name) {
        final Object attr = contents.get(name);
        if (attr instanceof PropertyWrapper) {
            ((PropertyWrapper)attr).deleter.run();
            return;
        }
        throw new IllegalArgumentException("Cannot delete function " + name);
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
