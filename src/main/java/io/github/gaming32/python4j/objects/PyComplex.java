package io.github.gaming32.python4j.objects;

public class PyComplex extends PyObject {
    private final double real;
    private final double imag;

    private PyComplex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    public static PyComplex fromDoubles(double real, double imag) {
        return new PyComplex(real, imag);
    }

    @Override
    public String __repr__() {
        return "(" + real + "+" + imag + "j)";
    }

    @Override
    public boolean __bool__() {
        return real != 0 || imag != 0;
    }
}
