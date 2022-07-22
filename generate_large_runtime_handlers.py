from io import StringIO
from typing import Any

GENERATED_HEADER = '// region GENERATED CODE (see generate_large_runtime_handlers.py)'
PATH = 'src/main/java/io/github/gaming32/python4j/runtime/PyRuntime.java'

with open(PATH, 'r') as fp:
    original = fp.read()

original = original[:original.find(GENERATED_HEADER)]

with open(PATH, 'w') as fp:
    fp.write(original)

    newline = True
    def prn(*args: Any, sep: str = ' ', end: str = '\n') -> None:
        global newline
        line = sep.join(map(str, args))
        if line:
            if newline:
                fp.write('    ')
                newline = False
            fp.write(line)
        fp.write(end)
        newline = bool(end)

    fp.write(GENERATED_HEADER)
    fp.write('\n')


    for i in range(2, 16):
        prn('public static PyObject buildTuple(PyObject v0', end='')
        for j in range(1, i):
            prn(f', PyObject v{j}', end='')
        prn(') {')
        prn('    return PyTuple.fromElements(v0', end='')
        for j in range(1, i):
            prn(f', v{j}', end='')
        prn(');')
        prn('}')
        prn()


    for i in range(2, 16):
        prn('public static PyObject buildList(PyObject v0', end='')
        for j in range(1, i):
            prn(f', PyObject v{j}', end='')
        prn(') {')
        prn(f'    final PyList result = PyList.fromSize({i});')
        for j in range(i):
            prn(f'    result.setItem({j}, v{j});')
        prn('    return result;')
        prn('}')
        prn()


    for i in range(2, 16):
        prn('public static PyObject buildSet(PyObject v0', end='')
        for j in range(1, i):
            prn(f', PyObject v{j}', end='')
        prn(') {')
        prn(f'    final PySet result = PySet.empty();')
        for j in range(i):
            prn(f'    result.add(v{j});')
        prn('    return result;')
        prn('}')
        prn()


    for i in range(2, 16):
        prn('public static PyObject buildMap(PyObject k0, PyObject v0', end='')
        for j in range(1, i):
            prn(f', PyObject k{j}, PyObject v{j}', end='')
        prn(') {')
        prn(f'    final PyDict result = PyDict.empty();')
        for j in range(i):
            prn(f'    result.setItem(k{j}, v{j});')
        prn('    return result;')
        prn('}')
        prn()


    for i in range(2, 16):
        prn('public static PyObject buildConstKeyMap(', end='')
        for j in range(i):
            prn(f'PyObject v{j}, ', end='')
        prn('PyObject keys) {')
        prn('    final PyTuple keyTuple = (PyTuple)keys;')
        prn(f'    final PyDict result = PyDict.empty();')
        for j in range(i):
            prn(f'    result.setItem(keyTuple.getItem({j}), v{j});')
        prn('    return result;')
        prn('}')
        prn()


    # public static PyObject call(PyObject o1, PyObject o2, PyObject o3) {
    #     return o2.__call__(new PyArguments(kwNames, o3));
    # }
    for i in range(3, 16):
        prn('public static PyObject call(PyObject o1, PyObject o2', end='')
        for j in range(3, i + 1):
            prn(f', PyObject o{j}', end='')
        prn(') {')
        prn('    return o1.__call__(new PyArguments(kwNames, o2', end='')
        for j in range(3, i + 1):
            prn(f', o{j}', end='')
        prn('));')
        prn('}')
        prn()


    prn('// endregion GENERATED CODE')
    fp.write('}\n')
