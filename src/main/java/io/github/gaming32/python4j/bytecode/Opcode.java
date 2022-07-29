package io.github.gaming32.python4j.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.gaming32.python4j.Utils;

public final class Opcode {
    public static final List<String> CMP_OP = List.of("<", "<=", "==", "!=", ">", ">=");
    public static final List<Integer> HAS_ARG;
    public static final List<Integer> HAS_CONST;
    public static final List<Integer> HAS_NAME;
    public static final List<Integer> HAS_JREL;
    public static final List<Integer> HAS_JABS;
    public static final List<Integer> HAS_LOCAL;
    public static final List<Integer> HAS_COMPARE;
    public static final List<Integer> HAS_FREE;
    public static final List<Integer> HAS_NARGS = List.of();
    public static final List<String> OP_NAME;
    public static final Map<String, Integer> OP_MAP;

    public static final int HAVE_ARGUMENT;

    public static final int CO_OPTIMIZED = 1;
    public static final int CO_NEWLOCALS = 2;
    public static final int CO_VARARGS = 4;
    public static final int CO_VARKEYWORDS = 8;
    public static final int CO_NESTED = 16;
    public static final int CO_GENERATOR = 32;
    public static final int CO_NOFREE = 64;
    public static final int CO_COROUTINE = 128;
    public static final int CO_ITERABLE_COROUTINE = 256;
    public static final int CO_ASYNC_GENERATOR = 512;

    public static final int MKFN_DEFAULTS = 0x01;
    public static final int MKFN_KWDEFAULTS = 0x02;
    public static final int MKFN_ANNOTATIONS = 0x04;
    public static final int MKFN_CLOSURE = 0x08;

    // region GENERATED CODE (see generate_opcode_fields.py);
    public static final int CACHE = 0;
    public static final int POP_TOP = 1;
    public static final int PUSH_NULL = 2;
    public static final int NOP = 9;
    public static final int UNARY_POSITIVE = 10;
    public static final int UNARY_NEGATIVE = 11;
    public static final int UNARY_NOT = 12;
    public static final int UNARY_INVERT = 15;
    public static final int BINARY_SUBSCR = 25;
    public static final int GET_LEN = 30;
    public static final int MATCH_MAPPING = 31;
    public static final int MATCH_SEQUENCE = 32;
    public static final int MATCH_KEYS = 33;
    public static final int PUSH_EXC_INFO = 35;
    public static final int CHECK_EXC_MATCH = 36;
    public static final int CHECK_EG_MATCH = 37;
    public static final int WITH_EXCEPT_START = 49;
    public static final int GET_AITER = 50;
    public static final int GET_ANEXT = 51;
    public static final int BEFORE_ASYNC_WITH = 52;
    public static final int BEFORE_WITH = 53;
    public static final int END_ASYNC_FOR = 54;
    public static final int STORE_SUBSCR = 60;
    public static final int DELETE_SUBSCR = 61;
    public static final int GET_ITER = 68;
    public static final int GET_YIELD_FROM_ITER = 69;
    public static final int PRINT_EXPR = 70;
    public static final int LOAD_BUILD_CLASS = 71;
    public static final int LOAD_ASSERTION_ERROR = 74;
    public static final int RETURN_GENERATOR = 75;
    public static final int LIST_TO_TUPLE = 82;
    public static final int RETURN_VALUE = 83;
    public static final int IMPORT_STAR = 84;
    public static final int SETUP_ANNOTATIONS = 85;
    public static final int YIELD_VALUE = 86;
    public static final int ASYNC_GEN_WRAP = 87;
    public static final int PREP_RERAISE_STAR = 88;
    public static final int POP_EXCEPT = 89;
    public static final int STORE_NAME = 90;
    public static final int DELETE_NAME = 91;
    public static final int UNPACK_SEQUENCE = 92;
    public static final int FOR_ITER = 93;
    public static final int UNPACK_EX = 94;
    public static final int STORE_ATTR = 95;
    public static final int DELETE_ATTR = 96;
    public static final int STORE_GLOBAL = 97;
    public static final int DELETE_GLOBAL = 98;
    public static final int SWAP = 99;
    public static final int LOAD_CONST = 100;
    public static final int LOAD_NAME = 101;
    public static final int BUILD_TUPLE = 102;
    public static final int BUILD_LIST = 103;
    public static final int BUILD_SET = 104;
    public static final int BUILD_MAP = 105;
    public static final int LOAD_ATTR = 106;
    public static final int COMPARE_OP = 107;
    public static final int IMPORT_NAME = 108;
    public static final int IMPORT_FROM = 109;
    public static final int JUMP_FORWARD = 110;
    public static final int JUMP_IF_FALSE_OR_POP = 111;
    public static final int JUMP_IF_TRUE_OR_POP = 112;
    public static final int POP_JUMP_FORWARD_IF_FALSE = 114;
    public static final int POP_JUMP_FORWARD_IF_TRUE = 115;
    public static final int LOAD_GLOBAL = 116;
    public static final int IS_OP = 117;
    public static final int CONTAINS_OP = 118;
    public static final int RERAISE = 119;
    public static final int COPY = 120;
    public static final int BINARY_OP = 122;
    public static final int SEND = 123;
    public static final int LOAD_FAST = 124;
    public static final int STORE_FAST = 125;
    public static final int DELETE_FAST = 126;
    public static final int POP_JUMP_FORWARD_IF_NOT_NONE = 128;
    public static final int POP_JUMP_FORWARD_IF_NONE = 129;
    public static final int RAISE_VARARGS = 130;
    public static final int GET_AWAITABLE = 131;
    public static final int MAKE_FUNCTION = 132;
    public static final int BUILD_SLICE = 133;
    public static final int JUMP_BACKWARD_NO_INTERRUPT = 134;
    public static final int MAKE_CELL = 135;
    public static final int LOAD_CLOSURE = 136;
    public static final int LOAD_DEREF = 137;
    public static final int STORE_DEREF = 138;
    public static final int DELETE_DEREF = 139;
    public static final int JUMP_BACKWARD = 140;
    public static final int CALL_FUNCTION_EX = 142;
    public static final int EXTENDED_ARG = 144;
    public static final int LIST_APPEND = 145;
    public static final int SET_ADD = 146;
    public static final int MAP_ADD = 147;
    public static final int LOAD_CLASSDEREF = 148;
    public static final int COPY_FREE_VARS = 149;
    public static final int RESUME = 151;
    public static final int MATCH_CLASS = 152;
    public static final int FORMAT_VALUE = 155;
    public static final int BUILD_CONST_KEY_MAP = 156;
    public static final int BUILD_STRING = 157;
    public static final int LOAD_METHOD = 160;
    public static final int LIST_EXTEND = 162;
    public static final int SET_UPDATE = 163;
    public static final int DICT_MERGE = 164;
    public static final int DICT_UPDATE = 165;
    public static final int PRECALL = 166;
    public static final int CALL = 171;
    public static final int KW_NAMES = 172;
    public static final int POP_JUMP_BACKWARD_IF_NOT_NONE = 173;
    public static final int POP_JUMP_BACKWARD_IF_NONE = 174;
    public static final int POP_JUMP_BACKWARD_IF_FALSE = 175;
    public static final int POP_JUMP_BACKWARD_IF_TRUE = 176;

    public static final int CMP_LT = 0;
    public static final int CMP_LE = 1;
    public static final int CMP_EQ = 2;
    public static final int CMP_NE = 3;
    public static final int CMP_GE = 4;
    public static final int CMP_GT = 5;
    // endregion GENERATED CODE

    static {
        class OpcodeSetup {
            final List<Integer> hasArg = new ArrayList<>();
            final List<Integer> hasConst = new ArrayList<>();
            final List<Integer> hasName = new ArrayList<>();
            final List<Integer> hasJrel = new ArrayList<>();
            final List<Integer> hasJabs = new ArrayList<>();
            final List<Integer> hasLocal = new ArrayList<>();
            final List<Integer> hasCompare = new ArrayList<>();
            final List<Integer> hasFree = new ArrayList<>();

            final Map<String, Integer> opMap = new HashMap<>();
            final String[] opName = new String[256]; {
                for (int op = 0; op < opName.length; op++) {
                    opName[op] = "<" + op + ">";
                }
            }

            void defOp(String name, int op) {
                opName[op] = name;
                opMap.put(name, op);
            }

            void nameOp(String name, int op) {
                defOp(name, op);
                hasName.add(op);
            }

            void jrelOp(String name, int op) {
                defOp(name, op);
                hasJrel.add(op);
            }
        }
        final OpcodeSetup o = new OpcodeSetup();

        // Instruction opcodes for compiled code
        // Blank lines correspond to available opcodes

        o.defOp("CACHE", 0);
        o.defOp("POP_TOP", 1);
        o.defOp("PUSH_NULL", 2);

        o.defOp("NOP", 9);
        o.defOp("UNARY_POSITIVE", 10);
        o.defOp("UNARY_NEGATIVE", 11);
        o.defOp("UNARY_NOT", 12);

        o.defOp("UNARY_INVERT", 15);

        o.defOp("BINARY_SUBSCR", 25);

        o.defOp("GET_LEN", 30);
        o.defOp("MATCH_MAPPING", 31);
        o.defOp("MATCH_SEQUENCE", 32);
        o.defOp("MATCH_KEYS", 33);

        o.defOp("PUSH_EXC_INFO", 35);
        o.defOp("CHECK_EXC_MATCH", 36);
        o.defOp("CHECK_EG_MATCH", 37);

        o.defOp("WITH_EXCEPT_START", 49);
        o.defOp("GET_AITER", 50);
        o.defOp("GET_ANEXT", 51);
        o.defOp("BEFORE_ASYNC_WITH", 52);
        o.defOp("BEFORE_WITH", 53);
        o.defOp("END_ASYNC_FOR", 54);

        o.defOp("STORE_SUBSCR", 60);
        o.defOp("DELETE_SUBSCR", 61);

        o.defOp("GET_ITER", 68);
        o.defOp("GET_YIELD_FROM_ITER", 69);
        o.defOp("PRINT_EXPR", 70);
        o.defOp("LOAD_BUILD_CLASS", 71);

        o.defOp("LOAD_ASSERTION_ERROR", 74);
        o.defOp("RETURN_GENERATOR", 75);

        o.defOp("LIST_TO_TUPLE", 82);
        o.defOp("RETURN_VALUE", 83);
        o.defOp("IMPORT_STAR", 84);
        o.defOp("SETUP_ANNOTATIONS", 85);
        o.defOp("YIELD_VALUE", 86);
        o.defOp("ASYNC_GEN_WRAP", 87);
        o.defOp("PREP_RERAISE_STAR", 88);
        o.defOp("POP_EXCEPT", 89);

        HAVE_ARGUMENT = 90;              // Opcodes from here have an argument:

        o.nameOp("STORE_NAME", 90);       // Index in name list
        o.nameOp("DELETE_NAME", 91);      // ""
        o.defOp("UNPACK_SEQUENCE", 92);   // Number of tuple items
        o.jrelOp("FOR_ITER", 93);
        o.defOp("UNPACK_EX", 94);
        o.nameOp("STORE_ATTR", 95);       // Index in name list
        o.nameOp("DELETE_ATTR", 96);      // ""
        o.nameOp("STORE_GLOBAL", 97);     // ""
        o.nameOp("DELETE_GLOBAL", 98);    // ""
        o.defOp("SWAP", 99);
        o.defOp("LOAD_CONST", 100);       // Index in const list
        o.hasConst.add(100);
        o.nameOp("LOAD_NAME", 101);       // Index in name list
        o.defOp("BUILD_TUPLE", 102);      // Number of tuple items
        o.defOp("BUILD_LIST", 103);       // Number of list items
        o.defOp("BUILD_SET", 104);        // Number of set items
        o.defOp("BUILD_MAP", 105);        // Number of dict entries
        o.nameOp("LOAD_ATTR", 106);       // Index in name list
        o.defOp("COMPARE_OP", 107);       // Comparison operator
        o.hasCompare.add(107);
        o.nameOp("IMPORT_NAME", 108);     // Index in name list
        o.nameOp("IMPORT_FROM", 109);     // Index in name list
        o.jrelOp("JUMP_FORWARD", 110);    // Number of words to skip
        o.jrelOp("JUMP_IF_FALSE_OR_POP", 111); // Number of words to skip
        o.jrelOp("JUMP_IF_TRUE_OR_POP", 112);  // ""
        o.jrelOp("POP_JUMP_FORWARD_IF_FALSE", 114);
        o.jrelOp("POP_JUMP_FORWARD_IF_TRUE", 115);
        o.nameOp("LOAD_GLOBAL", 116);     // Index in name list
        o.defOp("IS_OP", 117);
        o.defOp("CONTAINS_OP", 118);
        o.defOp("RERAISE", 119);
        o.defOp("COPY", 120);
        o.defOp("BINARY_OP", 122);
        o.jrelOp("SEND", 123); // Number of bytes to skip
        o.defOp("LOAD_FAST", 124);        // Local variable number
        o.hasLocal.add(124);
        o.defOp("STORE_FAST", 125);       // Local variable number
        o.hasLocal.add(125);
        o.defOp("DELETE_FAST", 126);      // Local variable number
        o.hasLocal.add(126);
        o.jrelOp("POP_JUMP_FORWARD_IF_NOT_NONE", 128);
        o.jrelOp("POP_JUMP_FORWARD_IF_NONE", 129);
        o.defOp("RAISE_VARARGS", 130);    // Number of raise arguments (1, 2, or 3);
        o.defOp("GET_AWAITABLE", 131);
        o.defOp("MAKE_FUNCTION", 132);    // Flags
        o.defOp("BUILD_SLICE", 133);      // Number of items
        o.jrelOp("JUMP_BACKWARD_NO_INTERRUPT", 134); // Number of words to skip (backwards);
        o.defOp("MAKE_CELL", 135);
        o.hasFree.add(135);
        o.defOp("LOAD_CLOSURE", 136);
        o.hasFree.add(136);
        o.defOp("LOAD_DEREF", 137);
        o.hasFree.add(137);
        o.defOp("STORE_DEREF", 138);
        o.hasFree.add(138);
        o.defOp("DELETE_DEREF", 139);
        o.hasFree.add(139);
        o.jrelOp("JUMP_BACKWARD", 140);    // Number of words to skip (backwards);

        o.defOp("CALL_FUNCTION_EX", 142);  // Flags

        o.defOp("EXTENDED_ARG", 144);
        o.defOp("LIST_APPEND", 145);
        o.defOp("SET_ADD", 146);
        o.defOp("MAP_ADD", 147);
        o.defOp("LOAD_CLASSDEREF", 148);
        o.hasFree.add(148);
        o.defOp("COPY_FREE_VARS", 149);

        o.defOp("RESUME", 151);   // This must be kept in sync with deepfreeze.py
        o.defOp("MATCH_CLASS", 152);

        o.defOp("FORMAT_VALUE", 155);
        o.defOp("BUILD_CONST_KEY_MAP", 156);
        o.defOp("BUILD_STRING", 157);

        o.nameOp("LOAD_METHOD", 160);

        o.defOp("LIST_EXTEND", 162);
        o.defOp("SET_UPDATE", 163);
        o.defOp("DICT_MERGE", 164);
        o.defOp("DICT_UPDATE", 165);
        o.defOp("PRECALL", 166);

        o.defOp("CALL", 171);
        o.defOp("KW_NAMES", 172);
        o.hasConst.add(172);

        o.jrelOp("POP_JUMP_BACKWARD_IF_NOT_NONE", 173);
        o.jrelOp("POP_JUMP_BACKWARD_IF_NONE", 174);
        o.jrelOp("POP_JUMP_BACKWARD_IF_FALSE", 175);
        o.jrelOp("POP_JUMP_BACKWARD_IF_TRUE", 176);

        for (int op : o.opMap.values()) {
            if (op >= HAVE_ARGUMENT) {
                o.hasArg.add(op);
            }
        }

        HAS_ARG = List.copyOf(o.hasArg);
        HAS_CONST = List.copyOf(o.hasConst);
        HAS_NAME = List.copyOf(o.hasName);
        HAS_JREL = List.copyOf(o.hasJrel);
        HAS_JABS = List.copyOf(o.hasJabs);
        HAS_LOCAL = List.copyOf(o.hasLocal);
        HAS_COMPARE = List.copyOf(o.hasCompare);
        HAS_FREE = List.copyOf(o.hasFree);
        OP_NAME = List.of(o.opName);
        OP_MAP = Map.copyOf(o.opMap);
    }

    static final List<Map.Entry<String, String>> NB_OPS = List.of(
        Map.entry("NB_ADD", "+"),
        Map.entry("NB_AND", "&"),
        Map.entry("NB_FLOOR_DIVIDE", "//"),
        Map.entry("NB_LSHIFT", "<<"),
        Map.entry("NB_MATRIX_MULTIPLY", "@"),
        Map.entry("NB_MULTIPLY", "*"),
        Map.entry("NB_REMAINDER", "%"),
        Map.entry("NB_OR", "|"),
        Map.entry("NB_POWER", "**"),
        Map.entry("NB_RSHIFT", ">>"),
        Map.entry("NB_SUBTRACT", "-"),
        Map.entry("NB_TRUE_DIVIDE", "/"),
        Map.entry("NB_XOR", "^"),
        Map.entry("NB_INPLACE_ADD", "+="),
        Map.entry("NB_INPLACE_AND", "&="),
        Map.entry("NB_INPLACE_FLOOR_DIVIDE", "//="),
        Map.entry("NB_INPLACE_LSHIFT", "<<="),
        Map.entry("NB_INPLACE_MATRIX_MULTIPLY", "@="),
        Map.entry("NB_INPLACE_MULTIPLY", "*="),
        Map.entry("NB_INPLACE_REMAINDER", "%="),
        Map.entry("NB_INPLACE_OR", "|="),
        Map.entry("NB_INPLACE_POWER", "**="),
        Map.entry("NB_INPLACE_RSHIFT", ">>="),
        Map.entry("NB_INPLACE_SUBTRACT", "-="),
        Map.entry("NB_INPLACE_TRUE_DIVIDE", "/="),
        Map.entry("NB_INPLACE_XOR", "^=")
    );

    static final Map<String, String[]> SPECIALIZATIONS = Utils.orderedMapOfEntries(
        Map.entry("BINARY_OP", new String[] {
            "BINARY_OP_ADAPTIVE",
            "BINARY_OP_ADD_FLOAT",
            "BINARY_OP_ADD_INT",
            "BINARY_OP_ADD_UNICODE",
            "BINARY_OP_INPLACE_ADD_UNICODE",
            "BINARY_OP_MULTIPLY_FLOAT",
            "BINARY_OP_MULTIPLY_INT",
            "BINARY_OP_SUBTRACT_FLOAT",
            "BINARY_OP_SUBTRACT_INT",
        }),
        Map.entry("BINARY_SUBSCR", new String[] {
            "BINARY_SUBSCR_ADAPTIVE",
            "BINARY_SUBSCR_DICT",
            "BINARY_SUBSCR_GETITEM",
            "BINARY_SUBSCR_LIST_INT",
            "BINARY_SUBSCR_TUPLE_INT",
        }),
        Map.entry("CALL", new String[] {
            "CALL_ADAPTIVE",
            "CALL_PY_EXACT_ARGS",
            "CALL_PY_WITH_DEFAULTS",
        }),
        Map.entry("COMPARE_OP", new String[] {
            "COMPARE_OP_ADAPTIVE",
            "COMPARE_OP_FLOAT_JUMP",
            "COMPARE_OP_INT_JUMP",
            "COMPARE_OP_STR_JUMP",
        }),
        Map.entry("EXTENDED_ARG", new String[] {
            "EXTENDED_ARG_QUICK",
        }),
        Map.entry("JUMP_BACKWARD", new String[] {
            "JUMP_BACKWARD_QUICK",
        }),
        Map.entry("LOAD_ATTR", new String[] {
            "LOAD_ATTR_ADAPTIVE",
            "LOAD_ATTR_INSTANCE_VALUE",
            "LOAD_ATTR_MODULE",
            "LOAD_ATTR_SLOT",
            "LOAD_ATTR_WITH_HINT",
        }),
        Map.entry("LOAD_CONST", new String[] {
            "LOAD_CONST__LOAD_FAST",
        }),
        Map.entry("LOAD_FAST", new String[] {
            "LOAD_FAST__LOAD_CONST",
            "LOAD_FAST__LOAD_FAST",
        }),
        Map.entry("LOAD_GLOBAL", new String[] {
            "LOAD_GLOBAL_ADAPTIVE",
            "LOAD_GLOBAL_BUILTIN",
            "LOAD_GLOBAL_MODULE",
        }),
        Map.entry("LOAD_METHOD", new String[] {
            "LOAD_METHOD_ADAPTIVE",
            "LOAD_METHOD_CLASS",
            "LOAD_METHOD_MODULE",
            "LOAD_METHOD_NO_DICT",
            "LOAD_METHOD_WITH_DICT",
            "LOAD_METHOD_WITH_VALUES",
        }),
        Map.entry("PRECALL", new String[] {
            "PRECALL_ADAPTIVE",
            "PRECALL_BOUND_METHOD",
            "PRECALL_BUILTIN_CLASS",
            "PRECALL_BUILTIN_FAST_WITH_KEYWORDS",
            "PRECALL_METHOD_DESCRIPTOR_FAST_WITH_KEYWORDS",
            "PRECALL_NO_KW_BUILTIN_FAST",
            "PRECALL_NO_KW_BUILTIN_O",
            "PRECALL_NO_KW_ISINSTANCE",
            "PRECALL_NO_KW_LEN",
            "PRECALL_NO_KW_LIST_APPEND",
            "PRECALL_NO_KW_METHOD_DESCRIPTOR_FAST",
            "PRECALL_NO_KW_METHOD_DESCRIPTOR_NOARGS",
            "PRECALL_NO_KW_METHOD_DESCRIPTOR_O",
            "PRECALL_NO_KW_STR_1",
            "PRECALL_NO_KW_TUPLE_1",
            "PRECALL_NO_KW_TYPE_1",
            "PRECALL_PYFUNC",
        }),
        Map.entry("RESUME", new String[] {
            "RESUME_QUICK",
        }),
        Map.entry("STORE_ATTR", new String[] {
            "STORE_ATTR_ADAPTIVE",
            "STORE_ATTR_INSTANCE_VALUE",
            "STORE_ATTR_SLOT",
            "STORE_ATTR_WITH_HINT",
        }),
        Map.entry("STORE_FAST", new String[] {
            "STORE_FAST__LOAD_FAST",
            "STORE_FAST__STORE_FAST",
        }),
        Map.entry("STORE_SUBSCR", new String[] {
            "STORE_SUBSCR_ADAPTIVE",
            "STORE_SUBSCR_DICT",
            "STORE_SUBSCR_LIST_INT",
        }),
        Map.entry("UNPACK_SEQUENCE", new String[] {
            "UNPACK_SEQUENCE_ADAPTIVE",
            "UNPACK_SEQUENCE_LIST",
            "UNPACK_SEQUENCE_TUPLE",
            "UNPACK_SEQUENCE_TWO_TUPLE",
        })
    );

    static final List<String> SPECIALIZED_INSTRUCTIONS = new ArrayList<>();

    static {
        for (final var family : SPECIALIZATIONS.values()) {
            for (final String opcode : family) {
                SPECIALIZED_INSTRUCTIONS.add(opcode);
            }
        }
    }

    static final Map<String, Map<String, Integer>> CACHE_FORMAT = Map.ofEntries(
        Map.entry("LOAD_GLOBAL", Utils.orderedMapOf(
            "counter", 1,
            "index", 1,
            "module_keys_version", 2,
            "builtin_keys_version", 1
        )),
        Map.entry("BINARY_OP", Utils.orderedMapOf(
            "counter", 1
        )),
        Map.entry("UNPACK_SEQUENCE", Utils.orderedMapOf(
            "counter", 1
        )),
        Map.entry("COMPARE_OP", Utils.orderedMapOf(
            "counter", 1,
            "mask", 1
        )),
        Map.entry("BINARY_SUBSCR", Utils.orderedMapOf(
            "counter", 1,
            "type_version", 2,
            "func_version", 1
        )),
        Map.entry("LOAD_ATTR", Utils.orderedMapOf(
            "counter", 1,
            "version", 2,
            "keys_version", 2,
            "descr", 4
        )),
        Map.entry("STORE_ATTR", Utils.orderedMapOf(
            "counter", 1,
            "version", 2,
            "index", 1
        )),
        Map.entry("LOAD_METHOD", Utils.orderedMapOf(
            "counter", 1,
            "type_version", 2,
            "dict_offset", 1,
            "keys_version", 2,
            "descr", 4
        )),
        Map.entry("CALL", Utils.orderedMapOf(
            "counter", 1,
            "func_version", 2,
            "min_args", 1
        )),
        Map.entry("PRECALL", Utils.orderedMapOf(
            "counter", 1
        )),
        Map.entry("STORE_SUBSCR", Utils.orderedMapOf(
            "counter", 1
        ))
    );

    static final int[] INLINE_CACHE_ENTRIES = new int[256];

    static {
        for (int opcode = 0; opcode < 256; opcode++) {
            INLINE_CACHE_ENTRIES[opcode] = CACHE_FORMAT.getOrDefault(OP_NAME.get(opcode), Map.of()).values().stream().mapToInt(Integer::intValue).sum();
        }
    }

    private Opcode() {
    }

    public static boolean comparisonToBoolean(int comparison, int op) {
        switch (op) {
            case CMP_EQ: return comparison == 0;
            case CMP_NE: return comparison != 0;
            case CMP_LT: return comparison < 0;
            case CMP_GT: return comparison > 0;
            case CMP_LE: return comparison <= 0;
            case CMP_GE: return comparison >= 0;
            default: throw new IllegalArgumentException("comparisonToBoolean op should be an Opcode.CMP_* constant");
        }
    }
}
