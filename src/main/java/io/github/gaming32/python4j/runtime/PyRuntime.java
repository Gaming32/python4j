package io.github.gaming32.python4j.runtime;

import java.util.Map;

import io.github.gaming32.python4j.objects.PyBool;
import io.github.gaming32.python4j.objects.PyDict;
import io.github.gaming32.python4j.objects.PyList;
import io.github.gaming32.python4j.objects.PyObject;
import io.github.gaming32.python4j.objects.PySet;
import io.github.gaming32.python4j.objects.PyTuple;

public final class PyRuntime {
    private PyRuntime() {
    }

    public static void printExpr(PyObject obj) {
        System.out.println(obj.__repr__());
    }

    public static void storeGlobal(PyObject value, Map<String, PyObject> globals, String name) {
        globals.put(name, value);
    }

    public static PyObject buildTuple() {
        return PyTuple.fromElements();
    }

    public static PyObject buildTuple(PyObject v0) {
        return PyTuple.fromElements(v0);
    }

    public static PyObject buildList() {
        return PyList.fromSize(0);
    }

    public static PyObject buildList(PyObject v0) {
        final PyList result = PyList.fromSize(1);
        result.setItem(0, v0);
        return result;
    }

    public static PyObject buildSet(PyObject v0) {
        final PySet result = PySet.empty();
        result.add(v0);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        return result;
    }

    public static PyObject listToTuple(PyObject list) {
        return PyTuple.fromElements(((PyList)list).toArray());
    }

    public static PyObject is(PyObject a, PyObject b) {
        return PyBool.fromBoolean(a == b);
    }

    public static PyObject isNot(PyObject a, PyObject b) {
        return PyBool.fromBoolean(a != b);
    }

    // region GENERATED CODE (see generate_large_runtime_handlers.py)
    public static PyObject buildTuple(PyObject v0, PyObject v1) {
        return PyTuple.fromElements(v0, v1);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2) {
        return PyTuple.fromElements(v0, v1, v2);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3) {
        return PyTuple.fromElements(v0, v1, v2, v3);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4) {
        return PyTuple.fromElements(v0, v1, v2, v3, v4);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5) {
        return PyTuple.fromElements(v0, v1, v2, v3, v4, v5);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6) {
        return PyTuple.fromElements(v0, v1, v2, v3, v4, v5, v6);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7) {
        return PyTuple.fromElements(v0, v1, v2, v3, v4, v5, v6, v7);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8) {
        return PyTuple.fromElements(v0, v1, v2, v3, v4, v5, v6, v7, v8);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9) {
        return PyTuple.fromElements(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10) {
        return PyTuple.fromElements(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11) {
        return PyTuple.fromElements(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12) {
        return PyTuple.fromElements(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12, PyObject v13) {
        return PyTuple.fromElements(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    public static PyObject buildTuple(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12, PyObject v13, PyObject v14) {
        return PyTuple.fromElements(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    public static PyObject buildList(PyObject v0, PyObject v1) {
        final PyList result = PyList.fromSize(2);
        result.setItem(0, v0);
        result.setItem(1, v1);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2) {
        final PyList result = PyList.fromSize(3);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3) {
        final PyList result = PyList.fromSize(4);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4) {
        final PyList result = PyList.fromSize(5);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        result.setItem(4, v4);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5) {
        final PyList result = PyList.fromSize(6);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        result.setItem(4, v4);
        result.setItem(5, v5);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6) {
        final PyList result = PyList.fromSize(7);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        result.setItem(4, v4);
        result.setItem(5, v5);
        result.setItem(6, v6);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7) {
        final PyList result = PyList.fromSize(8);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        result.setItem(4, v4);
        result.setItem(5, v5);
        result.setItem(6, v6);
        result.setItem(7, v7);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8) {
        final PyList result = PyList.fromSize(9);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        result.setItem(4, v4);
        result.setItem(5, v5);
        result.setItem(6, v6);
        result.setItem(7, v7);
        result.setItem(8, v8);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9) {
        final PyList result = PyList.fromSize(10);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        result.setItem(4, v4);
        result.setItem(5, v5);
        result.setItem(6, v6);
        result.setItem(7, v7);
        result.setItem(8, v8);
        result.setItem(9, v9);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10) {
        final PyList result = PyList.fromSize(11);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        result.setItem(4, v4);
        result.setItem(5, v5);
        result.setItem(6, v6);
        result.setItem(7, v7);
        result.setItem(8, v8);
        result.setItem(9, v9);
        result.setItem(10, v10);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11) {
        final PyList result = PyList.fromSize(12);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        result.setItem(4, v4);
        result.setItem(5, v5);
        result.setItem(6, v6);
        result.setItem(7, v7);
        result.setItem(8, v8);
        result.setItem(9, v9);
        result.setItem(10, v10);
        result.setItem(11, v11);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12) {
        final PyList result = PyList.fromSize(13);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        result.setItem(4, v4);
        result.setItem(5, v5);
        result.setItem(6, v6);
        result.setItem(7, v7);
        result.setItem(8, v8);
        result.setItem(9, v9);
        result.setItem(10, v10);
        result.setItem(11, v11);
        result.setItem(12, v12);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12, PyObject v13) {
        final PyList result = PyList.fromSize(14);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        result.setItem(4, v4);
        result.setItem(5, v5);
        result.setItem(6, v6);
        result.setItem(7, v7);
        result.setItem(8, v8);
        result.setItem(9, v9);
        result.setItem(10, v10);
        result.setItem(11, v11);
        result.setItem(12, v12);
        result.setItem(13, v13);
        return result;
    }

    public static PyObject buildList(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12, PyObject v13, PyObject v14) {
        final PyList result = PyList.fromSize(15);
        result.setItem(0, v0);
        result.setItem(1, v1);
        result.setItem(2, v2);
        result.setItem(3, v3);
        result.setItem(4, v4);
        result.setItem(5, v5);
        result.setItem(6, v6);
        result.setItem(7, v7);
        result.setItem(8, v8);
        result.setItem(9, v9);
        result.setItem(10, v10);
        result.setItem(11, v11);
        result.setItem(12, v12);
        result.setItem(13, v13);
        result.setItem(14, v14);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        result.add(v4);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        result.add(v4);
        result.add(v5);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        result.add(v4);
        result.add(v5);
        result.add(v6);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        result.add(v4);
        result.add(v5);
        result.add(v6);
        result.add(v7);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        result.add(v4);
        result.add(v5);
        result.add(v6);
        result.add(v7);
        result.add(v8);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        result.add(v4);
        result.add(v5);
        result.add(v6);
        result.add(v7);
        result.add(v8);
        result.add(v9);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        result.add(v4);
        result.add(v5);
        result.add(v6);
        result.add(v7);
        result.add(v8);
        result.add(v9);
        result.add(v10);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        result.add(v4);
        result.add(v5);
        result.add(v6);
        result.add(v7);
        result.add(v8);
        result.add(v9);
        result.add(v10);
        result.add(v11);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        result.add(v4);
        result.add(v5);
        result.add(v6);
        result.add(v7);
        result.add(v8);
        result.add(v9);
        result.add(v10);
        result.add(v11);
        result.add(v12);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12, PyObject v13) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        result.add(v4);
        result.add(v5);
        result.add(v6);
        result.add(v7);
        result.add(v8);
        result.add(v9);
        result.add(v10);
        result.add(v11);
        result.add(v12);
        result.add(v13);
        return result;
    }

    public static PyObject buildSet(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12, PyObject v13, PyObject v14) {
        final PySet result = PySet.empty();
        result.add(v0);
        result.add(v1);
        result.add(v2);
        result.add(v3);
        result.add(v4);
        result.add(v5);
        result.add(v6);
        result.add(v7);
        result.add(v8);
        result.add(v9);
        result.add(v10);
        result.add(v11);
        result.add(v12);
        result.add(v13);
        result.add(v14);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3, PyObject k4, PyObject v4) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        result.setItem(k4, v4);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3, PyObject k4, PyObject v4, PyObject k5, PyObject v5) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        result.setItem(k4, v4);
        result.setItem(k5, v5);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3, PyObject k4, PyObject v4, PyObject k5, PyObject v5, PyObject k6, PyObject v6) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        result.setItem(k4, v4);
        result.setItem(k5, v5);
        result.setItem(k6, v6);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3, PyObject k4, PyObject v4, PyObject k5, PyObject v5, PyObject k6, PyObject v6, PyObject k7, PyObject v7) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        result.setItem(k4, v4);
        result.setItem(k5, v5);
        result.setItem(k6, v6);
        result.setItem(k7, v7);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3, PyObject k4, PyObject v4, PyObject k5, PyObject v5, PyObject k6, PyObject v6, PyObject k7, PyObject v7, PyObject k8, PyObject v8) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        result.setItem(k4, v4);
        result.setItem(k5, v5);
        result.setItem(k6, v6);
        result.setItem(k7, v7);
        result.setItem(k8, v8);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3, PyObject k4, PyObject v4, PyObject k5, PyObject v5, PyObject k6, PyObject v6, PyObject k7, PyObject v7, PyObject k8, PyObject v8, PyObject k9, PyObject v9) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        result.setItem(k4, v4);
        result.setItem(k5, v5);
        result.setItem(k6, v6);
        result.setItem(k7, v7);
        result.setItem(k8, v8);
        result.setItem(k9, v9);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3, PyObject k4, PyObject v4, PyObject k5, PyObject v5, PyObject k6, PyObject v6, PyObject k7, PyObject v7, PyObject k8, PyObject v8, PyObject k9, PyObject v9, PyObject k10, PyObject v10) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        result.setItem(k4, v4);
        result.setItem(k5, v5);
        result.setItem(k6, v6);
        result.setItem(k7, v7);
        result.setItem(k8, v8);
        result.setItem(k9, v9);
        result.setItem(k10, v10);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3, PyObject k4, PyObject v4, PyObject k5, PyObject v5, PyObject k6, PyObject v6, PyObject k7, PyObject v7, PyObject k8, PyObject v8, PyObject k9, PyObject v9, PyObject k10, PyObject v10, PyObject k11, PyObject v11) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        result.setItem(k4, v4);
        result.setItem(k5, v5);
        result.setItem(k6, v6);
        result.setItem(k7, v7);
        result.setItem(k8, v8);
        result.setItem(k9, v9);
        result.setItem(k10, v10);
        result.setItem(k11, v11);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3, PyObject k4, PyObject v4, PyObject k5, PyObject v5, PyObject k6, PyObject v6, PyObject k7, PyObject v7, PyObject k8, PyObject v8, PyObject k9, PyObject v9, PyObject k10, PyObject v10, PyObject k11, PyObject v11, PyObject k12, PyObject v12) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        result.setItem(k4, v4);
        result.setItem(k5, v5);
        result.setItem(k6, v6);
        result.setItem(k7, v7);
        result.setItem(k8, v8);
        result.setItem(k9, v9);
        result.setItem(k10, v10);
        result.setItem(k11, v11);
        result.setItem(k12, v12);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3, PyObject k4, PyObject v4, PyObject k5, PyObject v5, PyObject k6, PyObject v6, PyObject k7, PyObject v7, PyObject k8, PyObject v8, PyObject k9, PyObject v9, PyObject k10, PyObject v10, PyObject k11, PyObject v11, PyObject k12, PyObject v12, PyObject k13, PyObject v13) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        result.setItem(k4, v4);
        result.setItem(k5, v5);
        result.setItem(k6, v6);
        result.setItem(k7, v7);
        result.setItem(k8, v8);
        result.setItem(k9, v9);
        result.setItem(k10, v10);
        result.setItem(k11, v11);
        result.setItem(k12, v12);
        result.setItem(k13, v13);
        return result;
    }

    public static PyObject buildMap(PyObject k0, PyObject v0, PyObject k1, PyObject v1, PyObject k2, PyObject v2, PyObject k3, PyObject v3, PyObject k4, PyObject v4, PyObject k5, PyObject v5, PyObject k6, PyObject v6, PyObject k7, PyObject v7, PyObject k8, PyObject v8, PyObject k9, PyObject v9, PyObject k10, PyObject v10, PyObject k11, PyObject v11, PyObject k12, PyObject v12, PyObject k13, PyObject v13, PyObject k14, PyObject v14) {
        final PyDict result = PyDict.empty();
        result.setItem(k0, v0);
        result.setItem(k1, v1);
        result.setItem(k2, v2);
        result.setItem(k3, v3);
        result.setItem(k4, v4);
        result.setItem(k5, v5);
        result.setItem(k6, v6);
        result.setItem(k7, v7);
        result.setItem(k8, v8);
        result.setItem(k9, v9);
        result.setItem(k10, v10);
        result.setItem(k11, v11);
        result.setItem(k12, v12);
        result.setItem(k13, v13);
        result.setItem(k14, v14);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        result.setItem(keyTuple.getItem(4), v4);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        result.setItem(keyTuple.getItem(4), v4);
        result.setItem(keyTuple.getItem(5), v5);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        result.setItem(keyTuple.getItem(4), v4);
        result.setItem(keyTuple.getItem(5), v5);
        result.setItem(keyTuple.getItem(6), v6);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        result.setItem(keyTuple.getItem(4), v4);
        result.setItem(keyTuple.getItem(5), v5);
        result.setItem(keyTuple.getItem(6), v6);
        result.setItem(keyTuple.getItem(7), v7);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        result.setItem(keyTuple.getItem(4), v4);
        result.setItem(keyTuple.getItem(5), v5);
        result.setItem(keyTuple.getItem(6), v6);
        result.setItem(keyTuple.getItem(7), v7);
        result.setItem(keyTuple.getItem(8), v8);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        result.setItem(keyTuple.getItem(4), v4);
        result.setItem(keyTuple.getItem(5), v5);
        result.setItem(keyTuple.getItem(6), v6);
        result.setItem(keyTuple.getItem(7), v7);
        result.setItem(keyTuple.getItem(8), v8);
        result.setItem(keyTuple.getItem(9), v9);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        result.setItem(keyTuple.getItem(4), v4);
        result.setItem(keyTuple.getItem(5), v5);
        result.setItem(keyTuple.getItem(6), v6);
        result.setItem(keyTuple.getItem(7), v7);
        result.setItem(keyTuple.getItem(8), v8);
        result.setItem(keyTuple.getItem(9), v9);
        result.setItem(keyTuple.getItem(10), v10);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        result.setItem(keyTuple.getItem(4), v4);
        result.setItem(keyTuple.getItem(5), v5);
        result.setItem(keyTuple.getItem(6), v6);
        result.setItem(keyTuple.getItem(7), v7);
        result.setItem(keyTuple.getItem(8), v8);
        result.setItem(keyTuple.getItem(9), v9);
        result.setItem(keyTuple.getItem(10), v10);
        result.setItem(keyTuple.getItem(11), v11);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        result.setItem(keyTuple.getItem(4), v4);
        result.setItem(keyTuple.getItem(5), v5);
        result.setItem(keyTuple.getItem(6), v6);
        result.setItem(keyTuple.getItem(7), v7);
        result.setItem(keyTuple.getItem(8), v8);
        result.setItem(keyTuple.getItem(9), v9);
        result.setItem(keyTuple.getItem(10), v10);
        result.setItem(keyTuple.getItem(11), v11);
        result.setItem(keyTuple.getItem(12), v12);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12, PyObject v13, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        result.setItem(keyTuple.getItem(4), v4);
        result.setItem(keyTuple.getItem(5), v5);
        result.setItem(keyTuple.getItem(6), v6);
        result.setItem(keyTuple.getItem(7), v7);
        result.setItem(keyTuple.getItem(8), v8);
        result.setItem(keyTuple.getItem(9), v9);
        result.setItem(keyTuple.getItem(10), v10);
        result.setItem(keyTuple.getItem(11), v11);
        result.setItem(keyTuple.getItem(12), v12);
        result.setItem(keyTuple.getItem(13), v13);
        return result;
    }

    public static PyObject buildConstKeyMap(PyObject v0, PyObject v1, PyObject v2, PyObject v3, PyObject v4, PyObject v5, PyObject v6, PyObject v7, PyObject v8, PyObject v9, PyObject v10, PyObject v11, PyObject v12, PyObject v13, PyObject v14, PyObject keys) {
        final PyTuple keyTuple = (PyTuple)keys;
        final PyDict result = PyDict.empty();
        result.setItem(keyTuple.getItem(0), v0);
        result.setItem(keyTuple.getItem(1), v1);
        result.setItem(keyTuple.getItem(2), v2);
        result.setItem(keyTuple.getItem(3), v3);
        result.setItem(keyTuple.getItem(4), v4);
        result.setItem(keyTuple.getItem(5), v5);
        result.setItem(keyTuple.getItem(6), v6);
        result.setItem(keyTuple.getItem(7), v7);
        result.setItem(keyTuple.getItem(8), v8);
        result.setItem(keyTuple.getItem(9), v9);
        result.setItem(keyTuple.getItem(10), v10);
        result.setItem(keyTuple.getItem(11), v11);
        result.setItem(keyTuple.getItem(12), v12);
        result.setItem(keyTuple.getItem(13), v13);
        result.setItem(keyTuple.getItem(14), v14);
        return result;
    }
    // endregion GENERATED CODE
}
