package io.github.gaming32.python4j.objects;

public class PyCodeObject extends PyObject {
    public static final class Builder {
        private PyUnicode filename;
        private PyUnicode name;
        private PyUnicode qualname;
        private int flags;

        private PyBytes code;
        private int firstLineNo;
        private PyBytes lineTable;

        private PyTuple consts;
        private PyTuple names;

        private PyTuple localsPlusNames;
        private PyBytes localsPlusKinds;

        private int argCount;
        private int posOnlyArgCount;
        private int kwOnlyArgCount;

        private int stackSize;

        private PyBytes exceptionTable;

        public Builder() {
        }

        public Builder filename(PyUnicode filename) {
            this.filename = filename;
            return this;
        }

        public Builder name(PyUnicode name) {
            this.name = name;
            return this;
        }

        public Builder qualname(PyUnicode qualname) {
            this.qualname = qualname;
            return this;
        }

        public Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        public Builder code(PyBytes code) {
            this.code = code;
            return this;
        }

        public Builder firstLineNo(int firstLineNo) {
            this.firstLineNo = firstLineNo;
            return this;
        }

        public Builder lineTable(PyBytes lineTable) {
            this.lineTable = lineTable;
            return this;
        }

        public Builder consts(PyTuple consts) {
            this.consts = consts;
            return this;
        }

        public Builder names(PyTuple names) {
            this.names = names;
            return this;
        }

        public Builder localsPlusNames(PyTuple localsPlusNames) {
            this.localsPlusNames = localsPlusNames;
            return this;
        }

        public Builder localsPlusKinds(PyBytes localsPlusKinds) {
            this.localsPlusKinds = localsPlusKinds;
            return this;
        }

        public Builder argCount(int argCount) {
            this.argCount = argCount;
            return this;
        }

        public Builder posOnlyArgCount(int posOnlyArgCount) {
            this.posOnlyArgCount = posOnlyArgCount;
            return this;
        }

        public Builder kwOnlyArgCount(int kwOnlyArgCount) {
            this.kwOnlyArgCount = kwOnlyArgCount;
            return this;
        }

        public Builder stackSize(int stackSize) {
            this.stackSize = stackSize;
            return this;
        }

        public Builder exceptionTable(PyBytes exceptionTable) {
            this.exceptionTable = exceptionTable;
            return this;
        }

        public Builder validate() {
            if (argCount < posOnlyArgCount || posOnlyArgCount < 0 ||
                kwOnlyArgCount < 0 ||
                stackSize < 0 || flags < 0 ||
                code == null ||
                consts == null ||
                names == null ||
                localsPlusNames == null ||
                localsPlusKinds == null ||
                localsPlusNames.length() != localsPlusKinds.length() ||
                name == null ||
                qualname == null ||
                filename == null ||
                lineTable == null ||
                exceptionTable == null
            ) {
                throw new IllegalArgumentException();
            }
            if (code.length() % 2 != 0) {
                throw new IllegalArgumentException("code: co_code is malformed");
            }
            int[] nLocals = new int[1];
            getLocalsPlusCounts(localsPlusNames, localsPlusKinds, nLocals);
            int nPlainLocals = nLocals[0] - argCount - kwOnlyArgCount
                - (((flags & CO_VARARGS) != 0) ? 1 : 0)
                - (((flags & CO_VARKEYWORDS) != 0) ? 1 : 0);
            if (nPlainLocals < 0) {
                throw new IllegalArgumentException("code: co_varnames is too small");
            }
            return this;
        }

        public PyCodeObject build() {
            validate();
            return new PyCodeObject(this);
        }

        public PyCodeObject buildInto(PyCodeObject code) {
            validate();
            code.buildFrom(this);
            return code;
        }
    }

    /* Masks for co_flags above */
    public static final int CO_OPTIMIZED   = 0x0001;
    public static final int CO_NEWLOCALS   = 0x0002;
    public static final int CO_VARARGS     = 0x0004;
    public static final int CO_VARKEYWORDS = 0x0008;
    public static final int CO_NESTED      = 0x0010;
    public static final int CO_GENERATOR   = 0x0020;

    public static final int CO_COROUTINE          = 0x0080;
    public static final int CO_ITERABLE_COROUTINE = 0x0100;
    public static final int CO_ASYNC_GENERATOR    = 0x0200;

    public static final int CO_FUTURE_DIVISION         = 0x20000;
    public static final int CO_FUTURE_ABSOLUTE_IMPORT  = 0x40000;
    public static final int CO_FUTURE_WITH_STATEMENT   = 0x80000;
    public static final int CO_FUTURE_PRINT_FUNCTION   = 0x100000;
    public static final int CO_FUTURE_UNICODE_LITERALS = 0x200000;

    public static final int CO_FUTURE_BARRY_AS_BDFL  = 0x400000;
    public static final int CO_FUTURE_GENERATOR_STOP = 0x800000;
    public static final int CO_FUTURE_ANNOTATIONS    = 0x1000000;

    public static final int CO_FAST_LOCAL = 0x20;
    public static final int CO_FAST_CELL  = 0x40;
    public static final int CO_FAST_FREE  = 0x80;

    private PyTuple co_consts;
    private PyTuple co_names;
    private PyBytes co_exceptiontable;
    private int co_flags;

    private int co_argcount;
    private int co_posonlyargcount;
    private int co_kwonlyargcount;
    private int co_stacksize;
    private int co_firstlineno;

    private int co_nlocalsplus;
    private int co_nlocals;
    private int co_nplaincellvars;
    private int co_ncellvars;
    private int co_nfreevars;

    private PyTuple co_localsplusnames;
    private PyBytes co_localspluskinds;
    private PyUnicode co_filename;
    private PyUnicode co_name;
    private PyUnicode co_qualname;
    private PyBytes co_linetable;
    private PyBytes co_code;

    public PyCodeObject() {
    }

    private PyCodeObject(Builder con) {
        buildFrom(con);
    }

    private void buildFrom(Builder con) {
        // TODO: intern strings
        int nLocalsPlus = con.localsPlusNames.length();
        int[] nLocalsValues = new int[4];
        getLocalsPlusCounts(con.localsPlusNames, con.localsPlusKinds, nLocalsValues);
        int nLocals = nLocalsValues[0];
        int nPlainCellVars = nLocalsValues[1];
        int nCellVars = nLocalsValues[2];
        int nFreeVars = nLocalsValues[3];

        co_filename = con.filename;
        co_name = con.name;
        co_qualname = con.qualname;
        co_flags = con.flags;

        co_firstlineno = con.firstLineNo;
        co_linetable = con.lineTable;

        co_consts = con.consts;
        co_names = con.names;

        co_localsplusnames = con.localsPlusNames;
        co_localspluskinds = con.localsPlusKinds;

        co_argcount = con.argCount;
        co_posonlyargcount = con.posOnlyArgCount;
        co_kwonlyargcount = con.kwOnlyArgCount;

        co_stacksize = con.stackSize;

        co_exceptiontable = con.exceptionTable;

        co_nlocalsplus = nLocalsPlus;
        co_nlocals = nLocals;
        co_nplaincellvars = nPlainCellVars;
        co_ncellvars = nCellVars;
        co_nfreevars = nFreeVars;

        co_code = con.code;
    }

    public PyTuple getCo_consts() {
        return co_consts;
    }

    public PyTuple getCo_names() {
        return co_names;
    }

    public PyBytes getCo_exceptiontable() {
        return co_exceptiontable;
    }

    public int getCo_flags() {
        return co_flags;
    }

    public int getCo_argcount() {
        return co_argcount;
    }

    public int getCo_posonlyargcount() {
        return co_posonlyargcount;
    }

    public int getCo_kwonlyargcount() {
        return co_kwonlyargcount;
    }

    public int getCo_stacksize() {
        return co_stacksize;
    }

    public int getCo_firstlineno() {
        return co_firstlineno;
    }

    public int getCo_nlocalsplus() {
        return co_nlocalsplus;
    }

    public int getCo_nlocals() {
        return co_nlocals;
    }

    public int getCo_nplaincellvars() {
        return co_nplaincellvars;
    }

    public int getCo_ncellvars() {
        return co_ncellvars;
    }

    public int getCo_nfreevars() {
        return co_nfreevars;
    }

    public PyTuple getCo_localsplusnames() {
        return co_localsplusnames;
    }

    public PyBytes getCo_localspluskinds() {
        return co_localspluskinds;
    }

    public PyUnicode getCo_filename() {
        return co_filename;
    }

    public PyUnicode getCo_name() {
        return co_name;
    }

    public PyUnicode getCo_qualname() {
        return co_qualname;
    }

    public PyBytes getCo_linetable() {
        return co_linetable;
    }

    public PyBytes getCo_code() {
        return co_code;
    }

    private static void getLocalsPlusCounts(PyTuple names, PyBytes kinds, int[] returnValues) {
        int nLocals = 0;
        int nPlainCellVars = 0;
        int nCellVars = 0;
        int nFreeVars = 0;
        int nLocalsPlus = names.length();
        byte[] rawKinds = kinds.toByteArray();
        for (int i = 0; i < nLocalsPlus; i++) {
            int kind = rawKinds[i] & 0xff;
            if ((kind & CO_FAST_LOCAL) != 0) {
                nLocals++;
                if ((kind & CO_FAST_CELL) != 0) {
                    nCellVars++;
                }
            } else if ((kind & CO_FAST_CELL) != 0) {
                nCellVars++;
                nPlainCellVars++;
            } else if ((kind & CO_FAST_FREE) != 0) {
                nFreeVars++;
            }
        }
        if (returnValues != null) {
            if (returnValues.length > 0) {
                returnValues[0] = nLocals;
            }
            if (returnValues.length > 1) {
                returnValues[1] = nPlainCellVars;
            }
            if (returnValues.length > 2) {
                returnValues[2] = nCellVars;
            }
            if (returnValues.length > 3) {
                returnValues[3] = nFreeVars;
            }
        }
    }
}
