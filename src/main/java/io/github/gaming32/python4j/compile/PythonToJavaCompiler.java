package io.github.gaming32.python4j.compile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import io.github.gaming32.python4j.bytecode.Disassemble;
import io.github.gaming32.python4j.bytecode.Disassemble.Instruction;
import io.github.gaming32.python4j.bytecode.Opcode;
import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.objects.PyNoneType;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.pycfile.MarshalWriter;
import io.github.gaming32.python4j.pycfile.PycFile;

public class PythonToJavaCompiler {
    private static enum ExtraGenerateKind {
        COPY, SWAP
    }

    private static final Pattern SAFE_NAME_REGEX = Pattern.compile("[.;\\[\\/<>]");
    static final String C_PYOBJECT = "io/github/gaming32/python4j/objects/PyObject";
    static final String C_PYCLASSINFO = "io/github/gaming32/python4j/runtime/annotation/PyClassInfo";
    static final String C_PYMETHODINFO = "io/github/gaming32/python4j/runtime/annotation/PyMethodInfo";
    static final String C_PYRUNTIME = "io/github/gaming32/python4j/runtime/PyRuntime";
    static final String C_CONDYBOOTSTRAPS = "io/github/gaming32/python4j/runtime/invoke/CondyBootstraps";
    static final String C_PYFRAME = "io/github/gaming32/python4j/runtime/PyFrame";

    private final String moduleName;
    private final PycFile pycFile;
    private final String className;
    private final ClassWriter result;
    private final Set<String> usedNames = new HashSet<>();
    private final MarshalWriter rootWriter = new MarshalWriter();
    private final MarshalWriter reusableWriter = new MarshalWriter();
    private final Map<Map.Entry<ExtraGenerateKind, Integer>, String> extraGenerate = new HashMap<>();
    private final Map<PyObject, ConstantDynamic> constantRefs = new HashMap<>();
    private int depth;

    public PythonToJavaCompiler(String moduleName, PycFile pycFile) {
        this.moduleName = moduleName;
        this.pycFile = pycFile;
        className = moduleName.replace('.', '/');
        result = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        initWriter();
    }

    private void initWriter() {
        result.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
        result.visitSource(getLastPathPart(pycFile.getCode().getCo_filename().toString()), null);
        {
            final AnnotationVisitor av = result.visitAnnotation("L" + C_PYCLASSINFO + ";", true);
            av.visit("metadata", pycFile.getMetadata());
            rootWriter.refAllCodeObjects();
            rootWriter.writeObject(pycFile.getCode());
            av.visit("codeObj", new String(rootWriter.getResult(), StandardCharsets.ISO_8859_1));
            av.visitEnd();
        }
        result.visitField(
            Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
            "$globals",
            "Ljava/util/Map;",
            "Ljava/util/Map<Ljava/lang/String;L" + C_PYOBJECT + ";>;",
            null
        );
        {
            final MethodVisitor mv = result.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        {
            final MethodVisitor mv = result.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            mv.visitTypeInsn(Opcodes.NEW, "java/util/LinkedHashMap");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/LinkedHashMap", "<init>", "()V", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, className, "$globals", "Ljava/util/Map;");
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
    }

    public static PythonToJavaCompiler compile(String moduleName, PycFile pycFile) {
        final PythonToJavaCompiler compiler = new PythonToJavaCompiler(moduleName, pycFile);
        compiler.compileCode(pycFile.getCode());
        return compiler;
    }

    public ClassWriter getResult() {
        return result;
    }

    public void compileCode(PyCodeObject codeObj) {
        depth++;
        for (final PyObject constant : codeObj.getCo_consts()) {
            if (constant instanceof PyCodeObject) {
                compileCode((PyCodeObject)constant);
            }
        }
        final String methodName = safeDeduppedName(codeObj.getCo_qualname().toString());
        final MethodVisitor mv = result.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            methodName,
            genericDescriptor(codeObj.getCo_argcount()),
            null, null
        );
        final InstructionAdapter meth = new InstructionAdapter(mv);
        final int refId = rootWriter.getRefId(codeObj);
        {
            final AnnotationVisitor av = mv.visitAnnotation("L" + C_PYMETHODINFO + ";", true);
            av.visit("codeRefId", refId);
            av.visitEnd();
        }
        mv.visitCode();
        final Label startLabel = new Label();
        meth.mark(startLabel);
        meth.aconst(Type.getObjectType(className));
        meth.iconst(refId);
        meth.invokestatic(
            C_PYFRAME,
            "push",
            "(Ljava/lang/Class;I)V",
            false
        );
        for (int i = 0; i < codeObj.getCo_argcount(); i++) {
            meth.visitVarInsn(Opcodes.ALOAD, i);
            meth.iconst(i);
            meth.invokestatic(
                C_PYFRAME,
                "storeFast",
                "(L" + C_PYOBJECT + ";I)V",
                false
            );
        }
        for (final Instruction insn : Disassemble.getInstructions(codeObj)) {
            if (insn.getStartsLine().isPresent()) {
                final Label lineLabel = new Label();
                meth.mark(lineLabel);
                meth.visitLineNumber(insn.getStartsLine().getAsInt(), lineLabel);
            }
            final int arg = insn.getArg().orElse(-1);
            switch (insn.getOpcode()) {
                case Opcode.NOP:
                case Opcode.RESUME:
                    break;

                case Opcode.POP_TOP:
                    // meth.pop();
                    // break;
                case Opcode.PRINT_EXPR:
                    invokeRuntime(meth, "printExpr", "(L" + C_PYOBJECT + ";)V");
                    break;

                case Opcode.COPY: switch (arg) {
                    case 0:
                        throw new IllegalArgumentException("Cannot COPY off the stack");
                    case 1:
                        meth.dup();
                        break;
                    case 2:
                        meth.swap();
                        meth.dupX1();
                        break;
                    case 3:
                        meth.dup2X1();
                        meth.pop2();
                        meth.dupX2();
                        break;
                    case 4:
                        meth.dup2X2();
                        meth.pop2();
                        meth.dup2X2();
                        meth.pop();
                        break;
                    default:
                        meth.invokestatic(
                            className,
                            generateKind(ExtraGenerateKind.COPY, arg),
                            genericDescriptor(arg),
                            false
                        );
                        copyArrayToStack(meth, arg, codeObj.getCo_nlocals());
                } break;

                case Opcode.SWAP: switch (arg) {
                    case 0:
                        throw new IllegalArgumentException("Cannot SWAP off the stack");
                    case 1:
                        meth.swap();
                        break;
                    case 2:
                        meth.swap();
                        meth.dupX1();
                        meth.pop2();
                        break;
                    default:
                        meth.invokestatic(
                            className,
                            generateKind(ExtraGenerateKind.SWAP, arg),
                            genericDescriptor(arg),
                            false
                        );
                        copyArrayToStack(meth, arg, codeObj.getCo_nlocals());
                } break;

                case Opcode.RETURN_VALUE:
                    meth.invokestatic(
                        C_PYFRAME,
                        "pop",
                        "()V",
                        false
                    );
                    meth.visitInsn(Opcodes.ARETURN);
                    break;

                case Opcode.STORE_NAME:
                    if (depth > 1) {
                        break;
                    }
                case Opcode.STORE_GLOBAL:
                    getGlobals(meth);
                    meth.aconst(codeObj.getCo_names().getItem(arg).toString());
                    invokeRuntime(meth, "storeGlobal", "(L" + C_PYOBJECT + ";Ljava/util/Map;Ljava/lang/String;)V");
                    break;

                case Opcode.DELETE_NAME:
                    if (depth > 1) {
                        break;
                    }
                case Opcode.DELETE_GLOBAL:
                    getGlobals(meth);
                    meth.aconst(codeObj.getCo_names().getItem(arg).toString());
                    meth.invokeinterface("java/util/Map", "remove", "(Ljava/lang/Object;)Ljava/lang/Object;");
                    meth.pop();
                    break;

                case Opcode.LOAD_CONST: {
                    final PyObject constant = codeObj.getCo_consts().getItem(arg);
                    if (constant == PyNoneType.PyNone) {
                        mv.visitFieldInsn(
                            Opcodes.GETSTATIC,
                            "io/github/gaming32/python4j/objects/PyNoneType",
                            "PyNone",
                            "Lio/github/gaming32/python4j/objects/PyNoneType;"
                        );
                    } else {
                        ConstantDynamic condy = constantRefs.get(constant);
                        if (condy == null) {
                            condy = new ConstantDynamic(
                                "$const$" + constantRefs.size(),
                                "L" + C_PYOBJECT + ";",
                                new Handle(
                                    Opcodes.H_INVOKESTATIC,
                                    C_CONDYBOOTSTRAPS,
                                    "constant",
                                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;II)L" + C_PYOBJECT + ";",
                                    false
                                ),
                                refId, arg
                            );
                            constantRefs.put(constant, condy);
                        }
                        meth.cconst(condy);
                    }
                    break;
                }

                case Opcode.BUILD_TUPLE:
                    invokeRuntime(meth, "buildTuple", genericDescriptor(arg));
                    break;

                case Opcode.BUILD_LIST:
                    invokeRuntime(meth, "buildList", genericDescriptor(arg));
                    break;

                case Opcode.BUILD_SET:
                    if (arg == 0) {
                        meth.invokestatic(
                            "io/github/gaming32/python4j/objects/PySet",
                            "empty",
                            "()Lio/github/gaming32/python4j/objects/PySet;",
                            false
                        );
                    } else {
                        invokeRuntime(meth, "buildList", genericDescriptor(arg));
                    }
                    break;

                case Opcode.BUILD_MAP:
                    if (arg == 0) {
                        meth.invokestatic(
                            "io/github/gaming32/python4j/objects/PyDict",
                            "empty",
                            "()Lio/github/gaming32/python4j/objects/PyDict;",
                            false
                        );
                    } else {
                        invokeRuntime(meth, "buildMap", genericDescriptor(arg * 2));
                    }
                    break;

                case Opcode.BUILD_CONST_KEY_MAP:
                    if (arg == 0) {
                        meth.invokestatic(
                            "io/github/gaming32/python4j/objects/PyDict",
                            "empty",
                            "()Lio/github/gaming32/python4j/objects/PyDict;",
                            false
                        );
                    } else {
                        invokeRuntime(meth, "buildConstKeyMap", genericDescriptor(arg + 1));
                    }
                    break;

                case Opcode.IS_OP:
                    invokeRuntime(meth, arg != 1 ? "is" : "isNot", "(L" + C_PYOBJECT + ";L" + C_PYOBJECT + ";)L" + C_PYOBJECT + ";");
                    break;

                case Opcode.LOAD_NAME:
                    if (depth > 1) {
                        break;
                    }
                case Opcode.LOAD_GLOBAL:
                    if ((arg & 1) != 0) {
                        meth.aconst(null);
                    }
                    getGlobals(meth);
                    meth.aconst(codeObj.getCo_names().getItem(arg >> 1).toString());
                    meth.invokeinterface(
                        "java/util/Map",
                        "get",
                        "(Ljava/lang/Object;)Ljava/lang/Object;"
                    );
                    meth.visitTypeInsn(Opcodes.CHECKCAST, C_PYOBJECT);
                    break;

                case Opcode.LOAD_FAST:
                    meth.visitVarInsn(Opcodes.ALOAD, arg);
                    break;

                case Opcode.DELETE_FAST:
                    meth.aconst(null);
                case Opcode.STORE_FAST:
                    meth.dup();
                    meth.visitVarInsn(Opcodes.ASTORE, arg);
                    meth.iconst(arg);
                    meth.invokestatic(
                        C_PYFRAME,
                        "storeFast",
                        "(L" + C_PYOBJECT + ";I)V",
                        false
                    );
                    break;

                case Opcode.PUSH_NULL:
                    meth.aconst(null);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported opcode: " + Opcode.OP_NAME.get(insn.getOpcode()));
            }
        }
        final Label endLabel = new Label();
        meth.mark(endLabel);
        for (int i = 0; i < codeObj.getCo_nlocals(); i++) {
            meth.visitLocalVariable(
                codeObj.getCo_varnames().getItem(i).toString(),
                "L" + C_PYOBJECT + ";",
                null,
                startLabel,
                endLabel,
                i
            );
        }
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
        depth--;
        if (depth == 0 && codeObj.getCo_argcount() == 0) {
            generateMainMethod(methodName);
        }
    }

    private void generateMainMethod(String moduleMethodName) {
        final MethodVisitor mv = result.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            className,
            moduleMethodName, // Probably "_module_0", but you can never be too careful
            "()L" + C_PYOBJECT + ";",
            false
        );
        mv.visitFieldInsn(
            Opcodes.GETSTATIC,
            "io/github/gaming32/python4j/objects/PyNoneType",
            "PyNone",
            "Lio/github/gaming32/python4j/objects/PyNoneType;"
        );
        final Label successLabel = new Label();
        mv.visitJumpInsn(Opcodes.IF_ACMPEQ, successLabel);
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/AssertionError");
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn("Top-level module code did not return None.");
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/AssertionError",
            "<init>",
            "(Ljava/lang/String;)V",
            false
        );
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitLabel(successLabel);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private static String safeName(String name) {
        return SAFE_NAME_REGEX.matcher(name).replaceAll("_");
    }

    private String safeDeduppedName(String name) {
        final String safeName = safeName(name);
        for (int i = 0;; i++) {
            final String deduppedName = safeName + i;
            if (usedNames.add(deduppedName)) {
                return deduppedName;
            }
        }
    }

    private String generateKind(ExtraGenerateKind kind, int n) {
        final var entry = Map.entry(kind, n);
        String methodName = extraGenerate.get(entry);
        if (methodName == null) {
            methodName = safeDeduppedName("$generated$" + kind + "$" + n + "$");
            extraGenerate.put(entry, methodName);
        }
        return methodName;
    }

    private static void copyArrayToStack(InstructionAdapter meth, int n, int localIdx) {
        if (n == 0) return;
        if (n > 1) {
            meth.dup();
            meth.visitVarInsn(Opcodes.ASTORE, localIdx);
        }
        meth.iconst(0);
        meth.visitInsn(Opcodes.AALOAD);
        for (int i = 1; i < n; i++) {
            meth.visitVarInsn(Opcodes.ALOAD, localIdx);
            meth.iconst(i);
            meth.visitInsn(Opcodes.AALOAD);
        }
    }

    private void getGlobals(InstructionAdapter meth) {
        meth.getstatic(className, "$globals", "Ljava/util/Map;");
    }

    private static void invokeRuntime(InstructionAdapter meth, String name, String desc) {
        meth.invokestatic(C_PYRUNTIME, name, desc, false);
    }

    private static String genericDescriptor(int nargs) {
        return "(" + ("L" + C_PYOBJECT + ";").repeat(nargs) + ")L" + C_PYOBJECT + ";";
    }

    private static String getLastPathPart(String path) {
        return path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
    }
}
