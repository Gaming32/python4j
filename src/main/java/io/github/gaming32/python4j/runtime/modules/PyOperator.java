package io.github.gaming32.python4j.runtime.modules;

import io.github.gaming32.python4j.objects.PyBool;
import io.github.gaming32.python4j.objects.PyNotImplemented;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.runtime.PyArguments;
import io.github.gaming32.python4j.runtime.javavirtualmodule.JavaVirtualModule;
import io.github.gaming32.python4j.runtime.javavirtualmodule.ModuleMethod;

public final class PyOperator extends JavaVirtualModule {
    public PyOperator() {
        super("_operator");
    }

    @ModuleMethod("truthy")
    public static PyObject pyTruthy(PyArguments args) {
        return PyBool.fromBoolean(truthy(args.getArg(0)));
    }

    public static boolean truthy(PyObject obj) {
        return obj.__bool__();
    }

    // region GENERATED CODE (see generate_nb_operator_overloads.py)
    @ModuleMethod("add")
    public static PyObject pyAdd(PyArguments args) {
        return add(args.getArg(0), args.getArg(1));
    }

    public static PyObject add(PyObject left, PyObject right) {
        PyObject result = left.__add__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__radd__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for +: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("and")
    public static PyObject pyAnd(PyArguments args) {
        return and(args.getArg(0), args.getArg(1));
    }

    public static PyObject and(PyObject left, PyObject right) {
        PyObject result = left.__and__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rand__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for &: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("floordiv")
    public static PyObject pyFloordiv(PyArguments args) {
        return floordiv(args.getArg(0), args.getArg(1));
    }

    public static PyObject floordiv(PyObject left, PyObject right) {
        PyObject result = left.__floordiv__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rfloordiv__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for //: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("lshift")
    public static PyObject pyLshift(PyArguments args) {
        return lshift(args.getArg(0), args.getArg(1));
    }

    public static PyObject lshift(PyObject left, PyObject right) {
        PyObject result = left.__lshift__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rlshift__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for <<: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("matmul")
    public static PyObject pyMatmul(PyArguments args) {
        return matmul(args.getArg(0), args.getArg(1));
    }

    public static PyObject matmul(PyObject left, PyObject right) {
        PyObject result = left.__matmul__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rmatmul__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for @: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("mul")
    public static PyObject pyMul(PyArguments args) {
        return mul(args.getArg(0), args.getArg(1));
    }

    public static PyObject mul(PyObject left, PyObject right) {
        PyObject result = left.__mul__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rmul__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for *: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("mod")
    public static PyObject pyMod(PyArguments args) {
        return mod(args.getArg(0), args.getArg(1));
    }

    public static PyObject mod(PyObject left, PyObject right) {
        PyObject result = left.__mod__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rmod__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for %: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("or")
    public static PyObject pyOr(PyArguments args) {
        return or(args.getArg(0), args.getArg(1));
    }

    public static PyObject or(PyObject left, PyObject right) {
        PyObject result = left.__or__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__ror__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for |: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("pow")
    public static PyObject pyPow(PyArguments args) {
        return pow(args.getArg(0), args.getArg(1));
    }

    public static PyObject pow(PyObject left, PyObject right) {
        PyObject result = left.__pow__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rpow__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for **: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("rshift")
    public static PyObject pyRshift(PyArguments args) {
        return rshift(args.getArg(0), args.getArg(1));
    }

    public static PyObject rshift(PyObject left, PyObject right) {
        PyObject result = left.__rshift__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rrshift__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for >>: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("sub")
    public static PyObject pySub(PyArguments args) {
        return sub(args.getArg(0), args.getArg(1));
    }

    public static PyObject sub(PyObject left, PyObject right) {
        PyObject result = left.__sub__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rsub__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for -: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("truediv")
    public static PyObject pyTruediv(PyArguments args) {
        return truediv(args.getArg(0), args.getArg(1));
    }

    public static PyObject truediv(PyObject left, PyObject right) {
        PyObject result = left.__truediv__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rtruediv__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for /: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("xor")
    public static PyObject pyXor(PyArguments args) {
        return xor(args.getArg(0), args.getArg(1));
    }

    public static PyObject xor(PyObject left, PyObject right) {
        PyObject result = left.__xor__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rxor__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for ^: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }

    @ModuleMethod("divmod")
    public static PyObject pyDivmod(PyArguments args) {
        return divmod(args.getArg(0), args.getArg(1));
    }

    public static PyObject divmod(PyObject left, PyObject right) {
        PyObject result = left.__divmod__(right);
        if (result == PyNotImplemented.NotImplemented) {
            result = right.__rdivmod__(left);
            if (result == PyNotImplemented.NotImplemented) {
                throw new UnsupportedOperationException("Unsupported operand types for divmod(): '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
            }
        }
        return result;
    }
    // endregion GENERATED CODE
}
