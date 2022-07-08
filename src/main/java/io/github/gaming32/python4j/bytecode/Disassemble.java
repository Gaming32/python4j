package io.github.gaming32.python4j.bytecode;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.gaming32.python4j.Utils;
import io.github.gaming32.python4j.objects.PyBool;
import io.github.gaming32.python4j.objects.PyCodeObject;
import io.github.gaming32.python4j.objects.PyCodeObject.PyCodeAddressRange;
import io.github.gaming32.python4j.objects.PyLong;
import io.github.gaming32.python4j.objects.PyNoneType;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.objects.PyTuple;
import io.github.gaming32.python4j.objects.PyUnicode;

public final class Disassemble {
    private static final class ExceptionTableEntry {
        final int start, end, target, depth;
        final boolean lasti;

        ExceptionTableEntry(int start, int end, int target, int depth, boolean lasti) {
            this.start = start;
            this.end = end;
            this.target = target;
            this.depth = depth;
            this.lasti = lasti;
        }

        @Override
        public String toString() {
            return "ExceptionTableEntry[depth=" + depth + ", end=" + end + ", lasti=" + lasti + ", start=" + start
                    + ", target=" + target + "]";
        }
    }

    public static final class Positions {
        private final int lineno, endLineno, colOffset, endColOffset;

        public Positions(int lineno, int endLineno, int colOffset, int endColOffset) {
            this.lineno = lineno;
            this.endLineno = endLineno;
            this.colOffset = colOffset;
            this.endColOffset = endColOffset;
        }

        public int getLineno() {
            return lineno;
        }

        public int getEndLineno() {
            return endLineno;
        }

        public int getColOffset() {
            return colOffset;
        }

        public int getEndColOffset() {
            return endColOffset;
        }

        @Override
        public String toString() {
            return "Positions[colOffset=" + colOffset + ", endColOffset=" + endColOffset + ", endLineno=" + endLineno
                    + ", lineno=" + lineno + "]";
        }
    }

    public static final class Instruction {
        /** Human readable name for operation */
        private final String opname;
        /** Numeric code for operation */
        private final int opcode;
        /** Numeric argument to operation (if any), otherwise empty */
        private final OptionalInt arg;
        /** Resolved arg value (if known), otherwise null */
        private final PyObject argval;
        /** Human readable description of operation argument */
        private final String argrepr;
        /** Start index of operation within bytecode sequence */
        private final int offset;
        /** Line started by this opcode (if any), otherwise empty */
        private final OptionalInt startsLine;
        /** True if other code jumps to here, otherwise False */
        private final boolean isJumpTarget;
        /** dis.Positions object holding the span of source code covered by this instruction, or null if there's no position information */
        private final Positions positions;

        public Instruction(String opname, int opcode, OptionalInt arg, PyObject argval, String argrepr, int offset,
                OptionalInt startsLine, boolean isJumpTarget, Positions positions) {
            this.opname = opname;
            this.opcode = opcode;
            this.arg = arg;
            this.argval = argval;
            this.argrepr = argrepr;
            this.offset = offset;
            this.startsLine = startsLine;
            this.isJumpTarget = isJumpTarget;
            this.positions = positions;
        }

        public String getOpname() {
            return opname;
        }

        public int getOpcode() {
            return opcode;
        }

        public OptionalInt getArg() {
            return arg;
        }

        public PyObject getArgval() {
            return argval;
        }

        public String getArgrepr() {
            return argrepr;
        }

        public int getOffset() {
            return offset;
        }

        public OptionalInt getStartsLine() {
            return startsLine;
        }

        public boolean isJumpTarget() {
            return isJumpTarget;
        }

        public Positions getPositions() {
            return positions;
        }

        private static final int OPNAME_WIDTH = 20;
        private static final int OPARG_WIDTH = 5;

        private String disassemble(int linenoWidth, int offsetWidth) {
            final List<String> fields = new ArrayList<>();
            if (linenoWidth != 0) {
                if (startsLine.isPresent()) {
                    fields.add(Utils.rightJustify(Integer.toString(startsLine.getAsInt()), linenoWidth));
                } else {
                    fields.add(" ".repeat(linenoWidth));
                }
            }
            fields.add("   ");
            if (isJumpTarget) {
                fields.add(">>");
            } else {
                fields.add("  ");
            }
            fields.add(Utils.rightJustify(Integer.toString(offset), offsetWidth));
            fields.add(Utils.leftJustify(opname, OPNAME_WIDTH));
            if (arg.isPresent()) {
                fields.add(Utils.rightJustify(Integer.toString(arg.getAsInt()), OPARG_WIDTH));
                if (argrepr != null && !argrepr.isEmpty()) {
                    fields.add("(" + argrepr + ")");
                }
            }
            return String.join(" ", fields).stripTrailing();
        }

        @Override
        public String toString() {
            return "Instruction[arg=" + arg + ", argrepr=" + argrepr + ", argval=" + argval + ", isJumpTarget="
                    + isJumpTarget + ", offset=" + offset + ", opcode=" + opcode + ", opname=" + opname + ", positions="
                    + positions + ", startsLine=" + startsLine + "]";
        }
    }

    private static final class OffsetOpArgTriple {
        final int offset, op;
        final OptionalInt arg;

        OffsetOpArgTriple(int offset, int op, OptionalInt arg) {
            this.offset = offset;
            this.op = op;
            this.arg = arg;
        }

        @Override
        public String toString() {
            return "OffsetOpArgTriple[arg=" + arg + ", op=" + op + ", offset=" + offset + "]";
        }
    }

    private static final PyObject UNKNOWN = new PyObject(); // Marker object

    private static final int FORMAT_VALUE = Opcode.OP_MAP.get("FORMAT_VALUE");
    private static final String[] FORMAT_VALUE_CONVERTERS = {"", "str", "repr", "ascii"};
    private static final int MAKE_FUNCTION = Opcode.OP_MAP.get("MAKE_FUNCTION");
    private static final String[] MAKE_FUNCTION_FLAGS = {"defaults", "kwdefaults", "annotations", "closure"};

    private static final int LOAD_CONST = Opcode.OP_MAP.get("LOAD_CONST");
    private static final int LOAD_GLOBAL = Opcode.OP_MAP.get("LOAD_GLOBAL");
    private static final int BINARY_OP = Opcode.OP_MAP.get("BINARY_OP");
    private static final int FOR_ITER = Opcode.OP_MAP.get("FOR_ITER");
    private static final int LOAD_ATTR = Opcode.OP_MAP.get("LOAD_ATTR");

    private static final String[] ALL_OPNAME = Opcode.OP_NAME.toArray(new String[0]);
    private static final Map<String, Integer> ALL_OPMAP = Opcode.OP_MAP;
    private static final Map<String, String> DEOPT_MAP = new HashMap<>();

    static {
        for (final var entry : Opcode.SPECIALIZATIONS.entrySet()) {
            for (final String specialized : entry.getValue()) {
                DEOPT_MAP.put(specialized, entry.getKey());
            }
        }
    }

    private Disassemble() {
    }

    public static void disassemble(PyCodeObject co, PrintStream out) {
        final Map<Integer, Integer> lineStarts = findLineStarts(co);
        final List<ExceptionTableEntry> exceptionEntries = parseExceptionTable(co);
        disassembleBytes0(
            co.getCo_code().toByteArray(), out,
            co::varnameFromOparg,
            co.getCo_names(), co.getCo_consts(),
            lineStarts, exceptionEntries
        );
    }

    public static void disassembleRecursive(PyCodeObject co, PrintStream out) {
        disassemble(co, out);
        for (final PyObject x : co.getCo_consts()) {
            if (x instanceof PyCodeObject) {
                out.println();
                out.println("Disassembly of " + x.__repr__() + ":");
                disassembleRecursive((PyCodeObject)x, out);
            }
        }
    }

    public static void disassembleBytes(byte[] code, PrintStream out) {
        disassembleBytes0(code, out, null, null, null, null, List.of());
    }

    private static void disassembleBytes0(
        byte[] code, PrintStream out,
        IntFunction<String> varnameFromOparg,
        PyTuple names, PyTuple co_consts,
        Map<Integer, Integer> lineStarts,
        List<ExceptionTableEntry> exceptionEntries
    ) {
        final boolean showLineno = lineStarts != null;
        final int linenoWidth;
        if (showLineno) {
            int maxLineno = 0;
            for (final int lineno : lineStarts.values()) {
                if (lineno > maxLineno) {
                    maxLineno = lineno;
                }
            }
            if (maxLineno >= 1000) {
                linenoWidth = Integer.toString(maxLineno).length();
            } else {
                linenoWidth = 3;
            }
        } else {
            linenoWidth = 0;
        }
        final int maxOffset = code.length - 2;
        final int offsetWidth;
        if (maxOffset >= 10000) {
            offsetWidth = Integer.toString(maxOffset).length();
        } else {
            offsetWidth = 4;
        }
        for (final Instruction instr : getInstructionsBytes(code, varnameFromOparg, names, co_consts, lineStarts, exceptionEntries)) {
            boolean newSourceLine = showLineno && instr.startsLine.isPresent() && instr.offset > 0;
            if (newSourceLine) {
                out.println();
            }
            out.println(instr.disassemble(linenoWidth, offsetWidth));
        }
        if (!exceptionEntries.isEmpty()) {
            out.println("ExceptionTable:");
            for (final ExceptionTableEntry entry : exceptionEntries) {
                final String lasti = entry.lasti ? " lasti" : "";
                final int end = entry.end - 2;
                out.println("  " + entry.start + " to " + end + " -> " + entry.target + " [" + entry.depth + "]" + lasti);
            }
        }
    }

    private static List<Instruction> getInstructionsBytes(
        byte[] code,
        IntFunction<String> varnameFromOparg,
        PyTuple names, PyTuple co_consts,
        Map<Integer, Integer> lineStarts,
        List<ExceptionTableEntry> exceptionEntries
    ) {
        final List<Instruction> result = new ArrayList<>();
        final Set<Integer> labels = new HashSet<>(findLabels(code));
        for (final ExceptionTableEntry entry : exceptionEntries) {
            for (int i = entry.start; i < entry.end; i++) {
                labels.add(entry.target);
            }
        }
        Integer startsLine = null;
        for (final OffsetOpArgTriple triple : unpackOpArgs(code)) {
            if (lineStarts != null) {
                startsLine = lineStarts.get(triple.offset);
            }
            final boolean isJumpTarget = labels.contains(triple.offset);
            PyObject argVal = null;
            String argRepr = "";
            final int deop = deoptop(triple.op);
            if (triple.arg.isPresent()) {
                final int arg = triple.arg.getAsInt();
                argVal = PyLong.fromInt(arg);
                if (Opcode.HAS_CONST.contains(deop)) {
                    final var constInfo = getConstInfo(deop, arg, co_consts);
                    argVal = constInfo.getKey();
                    argRepr = constInfo.getValue();
                } else if (Opcode.HAS_NAME.contains(deop)) {
                    if (deop == LOAD_GLOBAL) {
                        final var nameInfo = getNameInfo(arg / 2, names);
                        argVal = nameInfo.getKey();
                        argRepr = nameInfo.getValue();
                        if ((arg & 1) != 0 && !argRepr.isEmpty()) {
                            argRepr = "NULL + " + argRepr;
                        }
                    } else if (deop == LOAD_ATTR) {
                        final var nameInfo = getNameInfo(arg / 2, names);
                        argVal = nameInfo.getKey();
                        argRepr = nameInfo.getValue();
                        if ((arg & 1) != 0 && !argRepr.isEmpty()) {
                            argRepr = "NULL|self + " + argRepr;
                        }
                    } else {
                        final var nameInfo = getNameInfo(arg, names);
                        argVal = nameInfo.getKey();
                        argRepr = nameInfo.getValue();
                    }
                } else if (Opcode.HAS_JABS.contains(deop)) {
                    argVal = PyLong.fromInt(arg * 2);
                    argRepr = "to " + argVal.__repr__();
                } else if (Opcode.HAS_JREL.contains(deop)) {
                    final int signedArg = isBackwardJump(deop) ? -arg : arg;
                    int jArgVal = triple.offset + 2 + signedArg * 2;
                    if (deop == FOR_ITER) {
                        jArgVal += 2;
                    }
                    argVal = PyLong.fromInt(jArgVal);
                    argRepr = "to " + argVal.__repr__();
                } else if (Opcode.HAS_LOCAL.contains(deop) || Opcode.HAS_FREE.contains(deop)) {
                    final var nameInfo = getNameInfo(arg, names);
                    argVal = nameInfo.getKey();
                    argRepr = nameInfo.getValue();
                } else if (Opcode.HAS_COMPARE.contains(deop)) {
                    argRepr = Opcode.CMP_OP.get(arg);
                    argVal = PyUnicode.fromString(argRepr);
                } else if (deop == FORMAT_VALUE) {
                    argRepr = FORMAT_VALUE_CONVERTERS[arg & 0x3];
                    argVal = PyTuple.fromElements(
                        argRepr.isEmpty() ? PyNoneType.PyNone : PyUnicode.fromString(argRepr),
                        PyBool.fromBoolean((arg & 0x4) != 0)
                    );
                    if (((PyTuple)argVal).getItem(1).__bool__()) {
                        if (!argRepr.isEmpty()) {
                            argRepr += ", ";
                        }
                        argRepr += "with format";
                    }
                } else if (deop == MAKE_FUNCTION) {
                    argRepr = IntStream.range(0, MAKE_FUNCTION_FLAGS.length)
                        .filter(i -> (arg & (1 << i)) != 0)
                        .mapToObj(i -> MAKE_FUNCTION_FLAGS[i])
                        .collect(Collectors.joining(", "));
                } else if (deop == BINARY_OP) {
                    argRepr = Opcode.NB_OPS.get(arg).getValue();
                }
            }
            result.add(new Instruction(
                ALL_OPNAME[triple.op], triple.op, triple.arg,
                argVal, argRepr,
                triple.offset, Utils.boxedToOptional(startsLine), isJumpTarget,
                null
            ));
            final int caches = Opcode.INLINE_CACHE_ENTRIES[deop];
            if (caches == 0) continue;
            // We don't do caches or positions yet, so this is all a no-op
        }
        return result;
    }

    private static Map.Entry<PyObject, String> getNameInfo(int nameIndex, PyTuple names) {
        if (names != null) {
            final PyObject argVal = names.getItem(nameIndex);
            return Map.entry(argVal, ((PyUnicode)argVal).toString());
        }
        return Map.entry(UNKNOWN, "");
    }

    private static Map.Entry<PyObject, String> getConstInfo(int op, int arg, PyTuple co_consts) {
        final PyObject argVal = getConstValue(op, arg, co_consts);
        return Map.entry(argVal, argVal != UNKNOWN ? argVal.__repr__() : "");
    }

    private static PyObject getConstValue(int op, int arg, PyTuple co_consts) {
        assert Opcode.HAS_CONST.contains(op);
        PyObject argVal = UNKNOWN;
        if (op == LOAD_CONST) {
            if (co_consts != null) {
                argVal = co_consts.getItem(arg);
            }
        }
        return argVal;
    }

    private static List<Integer> findLabels(byte[] code) {
        final List<Integer> labels = new ArrayList<>();
        for (final OffsetOpArgTriple triple : unpackOpArgs(code)) {
            if (triple.arg.isPresent()) {
                final int deop = deoptop(triple.op);
                int label;
                if (Opcode.HAS_JREL.contains(deop)) {
                    int arg = triple.arg.getAsInt();
                    if (isBackwardJump(deop)) {
                        arg = -arg;
                    }
                    label = triple.offset + 2 + arg * 2;
                    if (deop == FOR_ITER) {
                        label += 2;
                    }
                } else if (Opcode.HAS_JABS.contains(deop)) {
                    label = triple.arg.getAsInt() * 2;
                } else {
                    continue;
                }
                if (!labels.contains(label)) {
                    labels.add(label);
                }
            }
        }
        return labels;
    }

    private static boolean isBackwardJump(int op) {
        return Opcode.OP_NAME.get(op).contains("JUMP_BACKWARD");
    }

    private static List<OffsetOpArgTriple> unpackOpArgs(byte[] code) {
        final List<OffsetOpArgTriple> result = new ArrayList<>();
        int extendedArg = 0;
        int caches = 0;
        for (int i = 0; i < code.length; i += 2) {
            if (caches != 0) {
                caches--;
                continue;
            }
            final int op = code[i] & 0xff;
            final int deop = deoptop(op);
            caches = Opcode.INLINE_CACHE_ENTRIES[deop];
            final OptionalInt arg;
            if (Opcode.HAS_ARG.contains(deop)) {
                arg = OptionalInt.of((code[i + 1] & 0xff) | extendedArg);
                extendedArg = deop == Opcode.EXTENDED_ARG ? arg.getAsInt() << 8 : 0;
            } else {
                arg = OptionalInt.empty();
                extendedArg = 0;
            }
            result.add(new OffsetOpArgTriple(i, op, arg));
        }
        return result;
    }

    private static int deoptop(int op) {
        final String deoptName = DEOPT_MAP.get(ALL_OPNAME[op]);
        return deoptName != null ? ALL_OPMAP.get(deoptName) : op;
    }

    private static Map<Integer, Integer> findLineStarts(PyCodeObject co) {
        final Map<Integer, Integer> lineStarts = new HashMap<>();
        int lastLine = -1;
        for (PyCodeAddressRange range : co.co_lines()) {
            final int line = range.getLine();
            if (line != -1 && line != lastLine) {
                lastLine = line;
                lineStarts.put(range.getStart(), line);
            }
        }
        return lineStarts;
    }

    private static List<ExceptionTableEntry> parseExceptionTable(PyCodeObject co) {
        final ByteArrayInputStream iterator = new ByteArrayInputStream(co.getCo_exceptiontable().toByteArray());
        final List<ExceptionTableEntry> entries = new ArrayList<>();
        try {
            while (true) {
                int start = parseVarint(iterator) * 2;
                int length = parseVarint(iterator) * 2;
                int end = start + length;
                int target = parseVarint(iterator) * 2;
                int dl = parseVarint(iterator);
                int depth = dl >>> 1;
                boolean lasti = (dl & 1) != 0;
                entries.add(new ExceptionTableEntry(start, end, target, depth, lasti));
            }
        } catch (NoSuchElementException e) {
            return entries;
        }
    }

    private static int parseVarint(ByteArrayInputStream iterator) {
        int b = iterator.read();
        if (b == -1) {
            throw new NoSuchElementException();
        }
        int val = b & 63;
        while ((b & 64) != 0) {
            val <<= 6;
            b = iterator.read();
            if (b == -1) {
                throw new NoSuchElementException();
            }
            val |= b & 63;
        }
        return val;
    }
}
