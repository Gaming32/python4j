package io.github.gaming32.python4j.objects;

import io.github.gaming32.python4j.bytecode.Opcode;
import io.github.gaming32.python4j.runtime.PyArguments;

public class PyObject {
    public PyUnicode __str__() {
        return __repr__();
    }

    public PyUnicode __repr__() {
        return PyUnicode.fromString("<" + getClass().getSimpleName() + " object 0x" + Integer.toHexString(hashCode()) + ">");
    }

    @Override
    public String toString() {
        return __str__().toString();
    }

    public long __hash__() {
        return System.identityHashCode(this);
    }

    protected final long notHashable() {
        throw new UnsupportedOperationException("Not a hashable type: " + this.getClass().getSimpleName());
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(__hash__());
    }

    public PyObject __call__(PyArguments args) {
        throw new UnsupportedOperationException();
    }

    public boolean __bool__() {
        return true;
    }

    public PyObject __richcmp__(PyObject other, int op) {
        return PyNotImplemented.NotImplemented;
    }

    // region GENERATED CODE (see generate_nb_operator_overloads.py)
    public PyObject __add__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __radd__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __and__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rand__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __floordiv__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rfloordiv__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __lshift__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rlshift__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __matmul__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rmatmul__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __mul__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rmul__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __mod__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rmod__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __or__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __ror__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __pow__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rpow__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rshift__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rrshift__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __sub__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rsub__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __truediv__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rtruediv__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __xor__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rxor__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __divmod__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __rdivmod__(PyObject other) {
        return PyNotImplemented.NotImplemented;
    }

    public PyObject __lt__(PyObject other) {
        return __richcmp__(other, Opcode.CMP_LT);
    }

    public PyObject __le__(PyObject other) {
        return __richcmp__(other, Opcode.CMP_LE);
    }

    public PyObject __eq__(PyObject other) {
        return __richcmp__(other, Opcode.CMP_EQ);
    }

    public PyObject __ne__(PyObject other) {
        return __richcmp__(other, Opcode.CMP_NE);
    }

    public PyObject __ge__(PyObject other) {
        return __richcmp__(other, Opcode.CMP_GE);
    }

    public PyObject __gt__(PyObject other) {
        return __richcmp__(other, Opcode.CMP_GT);
    }
    // endregion GENERATED CODE
}
