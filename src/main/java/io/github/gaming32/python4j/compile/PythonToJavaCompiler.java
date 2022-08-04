package io.github.gaming32.python4j.compile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
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
import io.github.gaming32.python4j.objects.PyBool;
import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.objects.PyEllipsisType;
import io.github.gaming32.python4j.objects.PyNoneType;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.objects.PyTuple;
import io.github.gaming32.python4j.pycfile.MarshalWriter;

public class PythonToJavaCompiler {
    private static enum ExtraGenerateKind {
        COPY, SWAP
    }

    private static final Pattern SAFE_NAME_REGEX = Pattern.compile("[.;\\[\\/<>]");
    private static final String[] GENERIC_DESCRIPTOR_CACHE = new String[256];
    private static final String[] NB_OP_NAMES = {
        "add",
        "and",
        "floordiv",
        "lshift",
        "matmul",
        "mul",
        "mod",
        "or",
        "pow",
        "rshift",
        "sub",
        "truediv",
        "xor",
    };
    private static final String[] CMP_OP_NAMES = {
        "lt",
        "le",
        "eq",
        "ne",
        "ge",
        "gt",
    };

    static final String C_PYOBJECT = "io/github/gaming32/python4j/objects/PyObject";
    static final String C_PYTUPLE = "io/github/gaming32/python4j/objects/PyTuple";
    static final String C_PYUNICODE = "io/github/gaming32/python4j/objects/PyUnicode";
    static final String C_PYCLASSINFO = "io/github/gaming32/python4j/runtime/annotation/PyClassInfo";
    static final String C_PYMETHODINFO = "io/github/gaming32/python4j/runtime/annotation/PyMethodInfo";
    static final String C_PYRUNTIME = "io/github/gaming32/python4j/runtime/PyRuntime";
    static final String C_CONDYBOOTSTRAPS = "io/github/gaming32/python4j/runtime/invoke/CondyBootstraps";
    static final String C_PYFRAME = "io/github/gaming32/python4j/runtime/PyFrame";
    static final String C_PYOPERATOR = "io/github/gaming32/python4j/runtime/modules/PyOperator";

    private static final String METHOD_DESCRIPTOR = "([L" + C_PYOBJECT + ";)L" + C_PYOBJECT + ";";

    private static final int FVC_MASK =      0x3;
    private static final int FVC_NONE =      0x0;
    private static final int FVC_STR =       0x1;
    private static final int FVC_REPR =      0x2;
    private static final int FVC_ASCII =     0x3;
    private static final int FVS_MASK =      0x4;
    private static final int FVS_HAVE_SPEC = 0x4;

    private final String moduleName;
    private final PyCodeObject codeObj;
    private final String className;
    private final ClassWriter result;
    private final Set<String> usedNames = new HashSet<>();
    private final MarshalWriter rootWriter = new MarshalWriter();
    private final MarshalWriter reusableWriter = new MarshalWriter();
    private final Map<Map.Entry<ExtraGenerateKind, Integer>, String> extraGenerate = new HashMap<>();
    private final Map<PyObject, ConstantDynamic> constantRefs = new HashMap<>();
    private final Map<PyCodeObject, String> methodNames = new IdentityHashMap<>(1);
    private int depth;
    private boolean generatedTopLevel;

    private PythonToJavaCompiler(String moduleName, PyCodeObject codeObj) {
        this.moduleName = moduleName;
        this.codeObj = codeObj;
        className = moduleName.replace('.', '/');
        result = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        initWriter();
    }

    private void initWriter() {
        result.visit(
            Opcodes.V11,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
            className,
            null,
            "java/lang/Object",
            new String[] {"io/github/gaming32/python4j/runtime/PyModule"}
        );
        result.visitSource(getLastPathPart(codeObj.getCo_filename().toString()), null);
        {
            final AnnotationVisitor av = result.visitAnnotation("L" + C_PYCLASSINFO + ";", true);
            rootWriter.refAllCodeObjects();
            rootWriter.writeObject(codeObj);
            av.visit("codeObj", new String(rootWriter.getResult(), StandardCharsets.ISO_8859_1));
            av.visitEnd();
        }
        result.visitField(
            Opcodes.ACC_PRIVATE,
            "$globals",
            "Ljava/util/Map;",
            "Ljava/util/Map<Ljava/lang/String;L" + C_PYOBJECT + ";>;",
            null
        );
        /* public <init>() */ {
            final MethodVisitor mv = result.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.NEW, "java/util/LinkedHashMap");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/LinkedHashMap", "<init>", "()V", false);
            mv.visitFieldInsn(Opcodes.PUTFIELD, className, "$globals", "Ljava/util/Map;");
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        /* public static void main(String[]) */ {
            final MethodVisitor mv = result.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
            mv.visitCode();
            mv.visitTypeInsn(Opcodes.NEW, className);
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", "()V", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "init", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        /* public String getName() */ {
            final MethodVisitor mv = result.visitMethod(Opcodes.ACC_PUBLIC, "getName", "()Ljava/lang/String;", null, null);
            mv.visitCode();
            mv.visitLdcInsn(moduleName);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        /* public String[] dir() */ {
            final MethodVisitor mv = result.visitMethod(Opcodes.ACC_PUBLIC, "dir", "()[Ljava/lang/String;", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, className, "$globals", "Ljava/util/Map;");
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, C_PYRUNTIME, "moduleDir", "(Ljava/util/Map;)[Ljava/lang/String;", false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        /* public PyObject getattr(String) */ {
            final MethodVisitor mv = result.visitMethod(
                Opcodes.ACC_PUBLIC,
                "getattr",
                "(Ljava/lang/String;)L" + C_PYOBJECT + ";",
                null, null
            );
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, className, "$globals", "Ljava/util/Map;");
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitTypeInsn(Opcodes.CHECKCAST, C_PYOBJECT);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        /* public boolean setattr(String, PyObject) */ {
            final MethodVisitor mv = result.visitMethod(
                Opcodes.ACC_PUBLIC,
                "setattr",
                "(Ljava/lang/String;L" + C_PYOBJECT + ";)Z",
                null, null
            );
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, className, "$globals", "Ljava/util/Map;");
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "set", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitInsn(Opcodes.POP);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        /* public String[] all() */ {
            final MethodVisitor mv = result.visitMethod(Opcodes.ACC_PUBLIC, "all", "()[Ljava/lang/String;", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, className, "$globals", "Ljava/util/Map;");
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, C_PYRUNTIME, "moduleAll", "(Ljava/util/Map;)[Ljava/lang/String;", false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
    }

    public static PythonToJavaCompiler compileModule(String moduleName, PyCodeObject codeObj) {
        final PythonToJavaCompiler compiler = new PythonToJavaCompiler(moduleName, codeObj);
        compiler.compileCode(codeObj);
        return compiler;
    }

    public ClassWriter getResult() {
        return result;
    }

    public String getModuleName() {
        return moduleName;
    }

    public PyCodeObject getCode() {
        return codeObj;
    }

    public String getClassName() {
        return className;
    }

    private void compileCode(PyCodeObject codeObj) {
        if (methodNames.containsKey(codeObj)) return;
        depth++;
        for (final PyObject constant : codeObj.getCo_consts()) {
            if (constant instanceof PyCodeObject) {
                compileCode((PyCodeObject)constant);
            }
        }
        final String methodName = safeDeduppedName(codeObj.getCo_qualname().toString());
        methodNames.put(codeObj, methodName);
        final MethodVisitor mv = result.visitMethod(
            Opcodes.ACC_PRIVATE,
            methodName,
            METHOD_DESCRIPTOR,
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
        meth.visitVarInsn(Opcodes.ALOAD, 1);
        meth.invokestatic(
            C_PYFRAME,
            "push",
            "(Ljava/lang/Class;I[L" + C_PYOBJECT + ";)[L" + C_PYOBJECT + ";",
            false
        );
        if (codeObj.getSumArgCount() == 0) {
            meth.pop();
        } else {
            int i;
            for (i = 0; i < codeObj.getSumArgCount() - 1; i++) {
                meth.dup();
                meth.iconst(i);
                meth.visitInsn(Opcodes.AALOAD);
                meth.visitVarInsn(Opcodes.ASTORE, i + 1);
            }
            meth.iconst(i);
            meth.visitInsn(Opcodes.AALOAD);
            meth.visitVarInsn(Opcodes.ASTORE, i + 1);
        }
        final Map<Integer, Label> jumpLabels = new HashMap<>();
        PyCodeObject lastCodeObject = null;
        for (final Instruction insn : Disassemble.getInstructions(codeObj)) {
            Label insnLabel = null;
            if (insn.isJumpTarget()) {
                insnLabel = jumpLabels.computeIfAbsent(insn.getOffset(), k -> new Label());
            }
            if (insnLabel == null && insn.getStartsLine().isPresent()) {
                insnLabel = new Label();
            }
            if (insnLabel != null) {
                meth.mark(insnLabel);
                if (insn.getStartsLine().isPresent()) {
                    meth.visitLineNumber(insn.getStartsLine().getAsInt(), insnLabel);
                }
            }
            final int op = Disassemble.deoptop(insn.getOpcode());
            final int arg = insn.getArg().orElse(-1);
            switch (op) {
                case Opcode.NOP:
                case Opcode.RESUME:
                case Opcode.PRECALL:
                case Opcode.EXTENDED_ARG: // These aren handled by getInstructions
                    break;

                case Opcode.POP_TOP:
                    meth.pop();
                    break;
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
                        break;
                    case 2:
                        meth.swap();
                        break;
                    case 3:
                        meth.swap();
                        meth.dup2X1();
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
                        throw new IllegalArgumentException("Cannot STORE_NAME from function yet");
                    }
                case Opcode.STORE_GLOBAL:
                    getGlobals(meth);
                    meth.aconst(codeObj.getCo_names().getItem(arg).toString());
                    invokeRuntime(meth, "storeGlobal", "(L" + C_PYOBJECT + ";Ljava/util/Map;Ljava/lang/String;)V");
                    break;

                case Opcode.DELETE_NAME:
                    if (depth > 1) {
                        throw new IllegalArgumentException("Cannot DELETE_NAME from function yet");
                    }
                case Opcode.DELETE_GLOBAL:
                    getGlobals(meth);
                    meth.aconst(codeObj.getCo_names().getItem(arg).toString());
                    meth.invokeinterface("java/util/Map", "remove", "(Ljava/lang/Object;)Ljava/lang/Object;");
                    meth.pop();
                    break;

                case Opcode.LOAD_CONST: {
                    final PyObject theConst = loadConst(meth, codeObj, refId, arg);
                    if (theConst instanceof PyCodeObject) {
                        lastCodeObject = (PyCodeObject)theConst;
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

                case Opcode.BUILD_STRING:
                    if (arg == 0) {
                        meth.invokestatic(C_PYUNICODE, "empty", "()L" + C_PYUNICODE + ";", false);
                    } else if (arg == 1) {
                        meth.invokevirtual(C_PYOBJECT, "__str__", "()L" + C_PYUNICODE + ";", false);
                    } else {
                        invokeRuntime(meth, "buildString", genericDescriptor(arg));
                    }
                    break;

                case Opcode.LIST_TO_TUPLE:
                    invokeRuntime(meth, "listToTuple", genericDescriptor(2));
                    break;

                case Opcode.LIST_EXTEND:
                    if (arg != 1) {
                        throw new IllegalArgumentException("LIST_EXTEND only supports argument of 1");
                    }
                    invokeRuntime(meth, "listExtend", genericDescriptor(2));
                    break;

                case Opcode.SET_UPDATE:
                    if (arg != 1) {
                        throw new IllegalArgumentException("SET_UPDATE only supports argument of 1");
                    }
                    invokeRuntime(meth, "setUpdate", genericDescriptor(2));
                    break;

                case Opcode.DICT_UPDATE:
                    if (arg != 1) {
                        throw new IllegalArgumentException("DICT_UPDATE only supports argument of 1");
                    }
                    invokeRuntime(meth, "dictUpdate", genericDescriptor(2));
                    break;

                case Opcode.DICT_MERGE:
                    if (arg != 1) {
                        throw new IllegalArgumentException("DICT_MERGE only supports argument of 1");
                    }
                    invokeRuntime(meth, "dictMerge", genericDescriptor(2));
                    break;

                case Opcode.LIST_APPEND:
                    if (arg != 1) {
                        throw new IllegalArgumentException("LIST_APPEND only supports argument of 1");
                    }
                    invokeRuntime(meth, "listAppend", genericDescriptor(2));
                    break;

                case Opcode.SET_ADD:
                    if (arg != 1) {
                        throw new IllegalArgumentException("SET_ADD only supports argument of 1");
                    }
                    invokeRuntime(meth, "setAdd", genericDescriptor(2));
                    break;

                case Opcode.MAP_ADD:
                    if (arg != 1) {
                        throw new IllegalArgumentException("MAP_ADD only supports argument of 1");
                    }
                    invokeRuntime(meth, "mapAdd", genericDescriptor(3));
                    break;

                case Opcode.IS_OP:
                    invokeRuntime(meth, arg != 1 ? "is" : "isNot", genericDescriptor(2));
                    break;

                case Opcode.JUMP_FORWARD:
                    meth.goTo(getJumpLabel(jumpLabels, insn, arg));
                    break;

                case Opcode.JUMP_BACKWARD:
                case Opcode.JUMP_BACKWARD_NO_INTERRUPT:
                    meth.goTo(getJumpLabel(jumpLabels, insn, -arg));
                    break;

                case Opcode.POP_JUMP_FORWARD_IF_TRUE:
                    meth.invokestatic(
                        C_PYOPERATOR,
                        "truthy",
                        "(L" + C_PYOBJECT + ";)Z",
                        false
                    );
                    meth.ifne(getJumpLabel(jumpLabels, insn, arg));
                    break;

                case Opcode.POP_JUMP_BACKWARD_IF_TRUE:
                    meth.invokestatic(
                        C_PYOPERATOR,
                        "truthy",
                        "(L" + C_PYOBJECT + ";)Z",
                        false
                    );
                    meth.ifne(getJumpLabel(jumpLabels, insn, -arg));
                    break;

                case Opcode.POP_JUMP_FORWARD_IF_FALSE:
                    meth.invokestatic(
                        C_PYOPERATOR,
                        "truthy",
                        "(L" + C_PYOBJECT + ";)Z",
                        false
                    );
                    meth.ifeq(getJumpLabel(jumpLabels, insn, arg));
                    break;

                case Opcode.POP_JUMP_BACKWARD_IF_FALSE:
                    meth.invokestatic(
                        C_PYOPERATOR,
                        "truthy",
                        "(L" + C_PYOBJECT + ";)Z",
                        false
                    );
                    meth.ifeq(getJumpLabel(jumpLabels, insn, -arg));
                    break;

                case Opcode.POP_JUMP_FORWARD_IF_NONE:
                    pushNone(mv);
                    meth.ifacmpeq(getJumpLabel(jumpLabels, insn, arg));
                    break;

                case Opcode.POP_JUMP_BACKWARD_IF_NONE:
                    pushNone(mv);
                    meth.ifacmpeq(getJumpLabel(jumpLabels, insn, -arg));
                    break;

                case Opcode.POP_JUMP_FORWARD_IF_NOT_NONE:
                    pushNone(mv);
                    meth.ifacmpne(getJumpLabel(jumpLabels, insn, arg));
                    break;

                case Opcode.POP_JUMP_BACKWARD_IF_NOT_NONE:
                    pushNone(mv);
                    meth.ifacmpne(getJumpLabel(jumpLabels, insn, -arg));
                    break;

                case Opcode.JUMP_IF_TRUE_OR_POP:
                    meth.dup();
                    meth.invokestatic(
                        C_PYOPERATOR,
                        "truthy",
                        "(L" + C_PYOBJECT + ";)Z",
                        false
                    );
                    meth.ifne(getJumpLabel(jumpLabels, insn, arg));
                    meth.pop();
                    break;

                case Opcode.JUMP_IF_FALSE_OR_POP:
                    meth.dup();
                    meth.invokestatic(
                        C_PYOPERATOR,
                        "truthy",
                        "(L" + C_PYOBJECT + ";)Z",
                        false
                    );
                    meth.ifeq(getJumpLabel(jumpLabels, insn, arg));
                    meth.pop();
                    break;

                case Opcode.LOAD_NAME:
                    if (depth > 1) {
                        throw new IllegalArgumentException("Cannot LOAD_NAME from function yet");
                    }
                case Opcode.LOAD_GLOBAL:
                    if ((arg & 1) != 0 && op == Opcode.LOAD_GLOBAL) {
                        meth.aconst(null);
                    }
                    getGlobals(meth);
                    meth.aconst(codeObj.getCo_names().getItem(op == Opcode.LOAD_GLOBAL ? arg >> 1 : arg).toString());
                    invokeRuntime(meth, "loadGlobal", "(Ljava/util/Map;Ljava/lang/String;)L" + C_PYOBJECT + ";");
                    break;

                case Opcode.LOAD_FAST:
                    meth.visitVarInsn(Opcodes.ALOAD, arg + 1);
                    break;

                case Opcode.DELETE_FAST:
                    meth.aconst(null);
                case Opcode.STORE_FAST:
                    meth.dup();
                    meth.visitVarInsn(Opcodes.ASTORE, arg + 1);
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

                case Opcode.KW_NAMES:
                    loadConst(meth, codeObj, refId, arg);
                    meth.visitTypeInsn(Opcodes.CHECKCAST, C_PYTUPLE);
                    meth.putstatic(C_PYRUNTIME, "kwNames", "L" + C_PYTUPLE + ";");
                    break;

                case Opcode.CALL:
                    invokeRuntime(meth, "call", genericDescriptor(arg + 2));
                    break;

                case Opcode.MAKE_FUNCTION: {
                    meth.visitVarInsn(Opcodes.ALOAD, 0);
                    meth.invokedynamic(
                        "apply",
                        "(L" + className + ";)Ljava/util/function/Function;",
                        new Handle(
                            Opcodes.H_INVOKESTATIC,
                            "java/lang/invoke/LambdaMetafactory",
                            "metafactory",
                            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                            false
                        ),
                        new Object[] {
                            Type.getMethodType("(Ljava/lang/Object;)Ljava/lang/Object;"),
                            new Handle(
                                Opcodes.H_INVOKEVIRTUAL,
                                className,
                                methodNames.get(lastCodeObject),
                                METHOD_DESCRIPTOR,
                                false
                            ),
                            Type.getMethodType(METHOD_DESCRIPTOR)
                        }
                    );
                    if (arg == 0) {
                        invokeRuntime(meth, "makeFunction", "(L" + C_PYOBJECT + ";Ljava/util/function/Function;)L" + C_PYOBJECT + ";");
                    } else {
                        int nargs = 1 + Integer.bitCount(arg);
                        final int clearedFlags = arg & ~(Opcode.MKFN_DEFAULTS | Opcode.MKFN_KWDEFAULTS | Opcode.MKFN_ANNOTATIONS);
                        if (clearedFlags != 0) {
                            throw new IllegalArgumentException("Unsupported MAKE_FUNCTION args: 0x" + Integer.toHexString(clearedFlags));
                        }
                        meth.iconst(arg);
                        invokeRuntime(meth, "makeFunction", "(" + ("L" + C_PYOBJECT + ";").repeat(nargs) + "Ljava/util/function/Function;I)L" + C_PYOBJECT + ";");
                    }
                    break;
                }

                case Opcode.BINARY_OP:
                    meth.invokestatic(C_PYOPERATOR, NB_OP_NAMES[arg], genericDescriptor(2), false);
                    break;

                case Opcode.COMPARE_OP:
                    meth.invokestatic(C_PYOPERATOR, CMP_OP_NAMES[arg], genericDescriptor(2), false);
                    break;

                case Opcode.FORMAT_VALUE: {
                    final int whichConversion = arg & FVC_MASK;
                    final boolean haveFmtSpec = (arg & FVS_MASK) == FVS_HAVE_SPEC;

                    final String convFn;
                    switch (whichConversion) {
                        case FVC_NONE: convFn = null; break;
                        case FVC_STR: convFn = "__str__"; break;
                        case FVC_REPR: convFn = "__repr__"; break;
                        default:
                            throw new IllegalArgumentException("Unsupported FORMAT_VALUE conversion: " + whichConversion);
                    }

                    if (convFn != null) {
                        if (haveFmtSpec) {
                            meth.swap();
                        }
                        meth.invokevirtual(C_PYOBJECT, convFn, "()L" + C_PYUNICODE + ";", false);
                        if (haveFmtSpec) {
                            meth.swap();
                        }
                    }

                    if (haveFmtSpec) {
                        meth.visitTypeInsn(Opcodes.CHECKCAST, C_PYUNICODE);
                    } else {
                        meth.aconst(null);
                    }
                    meth.invokestatic(
                        "io/github/gaming32/python4j/runtime/modules/PyBuiltins",
                        "format",
                        "(L" + C_PYOBJECT + ";L" + C_PYUNICODE + ";)L" + C_PYUNICODE + ";",
                        false
                    );
                    break;
                }

                default:
                    throw new IllegalArgumentException("Unsupported opcode: " + Opcode.OP_NAME.get(op));
            }
        }
        final Label endLabel = new Label();
        meth.mark(endLabel);
        meth.visitLocalVariable("this", "L" + className + ";", null, startLabel, endLabel, 0);
        final PyTuple co_varnames = codeObj.getCo_varnames();
        for (int i = 0; i < codeObj.getCo_nlocals(); i++) {
            meth.visitLocalVariable(
                co_varnames.getItem(i).toString(),
                "L" + C_PYOBJECT + ";",
                null,
                startLabel,
                endLabel,
                i + 1
            );
        }
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
        depth--;
        if (!generatedTopLevel && depth == 0 && codeObj.getSumArgCount() == 0) {
            generatedTopLevel = true;
            generateInitMethod(methodName);
        }
    }

    private void generateInitMethod(String methodName) {
        final MethodVisitor mv = result.visitMethod(Opcodes.ACC_PUBLIC, "init", "()V", null, null);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, C_PYOBJECT);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, methodName, METHOD_DESCRIPTOR, false);
        final Label successLabel = new Label();
        pushNone(mv);
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

    private static Label getJumpLabel(Map<Integer, Label> jumpLabels, Instruction insn, int arg) {
        return jumpLabels.computeIfAbsent(insn.getOffset() + 2 + arg * 2, k -> new Label());
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
        meth.visitVarInsn(Opcodes.ALOAD, 0);
        meth.getfield(className, "$globals", "Ljava/util/Map;");
    }

    private static void invokeRuntime(InstructionAdapter meth, String name, String desc) {
        meth.invokestatic(C_PYRUNTIME, name, desc, false);
    }

    private static String genericDescriptor(int nargs) {
        if (GENERIC_DESCRIPTOR_CACHE[nargs] == null) {
            return GENERIC_DESCRIPTOR_CACHE[nargs] = "(" + ("L" + C_PYOBJECT + ";").repeat(nargs) + ")L" + C_PYOBJECT + ";";
        }
        return GENERIC_DESCRIPTOR_CACHE[nargs];
    }

    private PyObject loadConst(InstructionAdapter meth, PyCodeObject codeObj, int refId, int constId) {
        final PyObject constant = codeObj.getCo_consts().getItem(constId);
        if (constant == PyNoneType.PyNone) {
            pushNone(meth);
        } else if (constant == PyEllipsisType.PyEllipsis) {
            meth.getstatic(
                "io/github/gaming32/python4j/objects/PyEllipsisType",
                "PyEllipsis",
                "Lio/github/gaming32/python4j/objects/PyEllipsisType;"
            );
        } else if (constant instanceof PyBool) {
            meth.getstatic(
                "io/github/gaming32/python4j/objects/PyBool",
                "Py" + constant,
                "Lio/github/gaming32/python4j/objects/PyBool;"
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
                    refId, constId
                );
                constantRefs.put(constant, condy);
            }
            meth.cconst(condy);
        }
        return constant;
    }

    private static void pushNone(MethodVisitor mv) {
        mv.visitFieldInsn(
            Opcodes.GETSTATIC,
            "io/github/gaming32/python4j/objects/PyNoneType",
            "PyNone",
            "Lio/github/gaming32/python4j/objects/PyNoneType;"
        );
    }

    private static String getLastPathPart(String path) {
        return File.separatorChar == '/' ?
            path.substring(path.lastIndexOf('/') + 1) :
            path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf(File.separatorChar)) + 1);
    }
}
