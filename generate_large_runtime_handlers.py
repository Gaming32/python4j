for i in range(2, 16):
    print('public static PyObject buildTuple(PyObject v0', end='')
    for j in range(1, i):
        print(f', PyObject v{j}', end='')
    print(') {')
    print('    return PyTuple.fromElements(v0', end='')
    for j in range(1, i):
        print(f', v{j}', end='')
    print(');')
    print('}')
    print()


for i in range(2, 16):
    print('public static PyObject buildList(PyObject v0', end='')
    for j in range(1, i):
        print(f', PyObject v{j}', end='')
    print(') {')
    print(f'    final PyList result = PyList.fromSize({i});')
    for j in range(i):
        print(f'    result.setItem({j}, v{j});')
    print('    return result;')
    print('}')
    print()


for i in range(2, 16):
    print('public static PyObject buildSet(PyObject v0', end='')
    for j in range(1, i):
        print(f', PyObject v{j}', end='')
    print(') {')
    print(f'    final PySet result = PySet.empty();')
    for j in range(i):
        print(f'    result.add(v{j});')
    print('    return result;')
    print('}')
    print()


for i in range(2, 16):
    print('public static PyObject buildMap(PyObject k0, PyObject v0', end='')
    for j in range(1, i):
        print(f', PyObject k{j}, PyObject v{j}', end='')
    print(') {')
    print(f'    final PyDict result = PyDict.empty();')
    for j in range(i):
        print(f'    result.setItem(k{j}, v{j});')
    print('    return result;')
    print('}')
    print()


for i in range(2, 16):
    print('public static PyObject buildConstKeyMap(', end='')
    for j in range(i):
        print(f'PyObject v{j}, ', end='')
    print('PyObject keys) {')
    print('    final PyTuple keyTuple = (PyTuple)keys;')
    print(f'    final PyDict result = PyDict.empty();')
    for j in range(i):
        print(f'    result.setItem(keyTuple.getItem({j}), v{j});')
    print('    return result;')
    print('}')
    print()


for i in range(2, 16):
    print('public static PyObject buildString(PyObject v0', end='')
