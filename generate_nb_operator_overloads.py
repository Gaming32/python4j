from opcode import _nb_ops  # type: ignore

oplist = [
    'add',
    'and',
    'floordiv',
    'lshift',
    'matmul',
    'mul',
    'mod',
    'or',
    'pow',
    'rshift',
    'sub',
    'truediv',
    'xor',
    'divmod'
]
oplabels: list[str] = [l[1] for l in _nb_ops[:13]] + [
    'divmod()',
]
cmpops: list[tuple[str, str]] = [
    ('<', 'lt'),
    ('<=', 'le'),
    ('==', 'eq'),
    ('!=', 'ne'),
    ('>=', 'ge'),
    ('>', 'gt')
]


print('// FOR: PyObject.java')

for name in oplist:
    for subname in ('', 'r'):
        print(f'public PyObject __{subname}{name}__(PyObject other) {{')
        print(f'    return PyNotImplemented.NotImplemented;')
        print('}')
        print()

for (op, name) in cmpops:
    print(f'public PyObject __{name}__(PyObject other) {{')
    print(f'    return __richcmp__(other, Opcode.CMP_{name.upper()});')
    print('}')
    print()


print('// FOR: PyOperator.java')

# @ModuleMethod("add")
# public static PyObject pyAdd(PyArguments args) {
#     return add(args.getArg(0), args.getArg(1));
# }
#
# public static PyObject add(PyObject left, PyObject right) {
#     PyObject result = left.__add__(right);
#     if (result == PyNotImplemented.NotImplemented) {
#         result = right.__radd__(left);
#         if (result == PyNotImplemented.NotImplemented) {
#             throw new UnsupportedOperationException("Unsupported operand types for +: '" + left.getClass().getSimpleName() + "' and '" + right.getClass().getSimpleName() + "'");
#         }
#     }
#     return result;
# }

for (name, label) in zip(oplist, oplabels):
    print(f'@ModuleMethod("{name}")')
    print(f'public static PyObject py{name.capitalize()}(PyArguments args) {{')
    print(f'    return {name}(args.getArg(0), args.getArg(1));')
    print('}')
    print()
    print(f'public static PyObject {name}(PyObject left, PyObject right) {{')
    print(f'    PyObject result = left.__{name}__(right);')
    print(f'    if (result == PyNotImplemented.NotImplemented) {{')
    print(f'        result = right.__r{name}__(left);')
    print(f'        if (result == PyNotImplemented.NotImplemented) {{')
    print(f'            throw new UnsupportedOperationException("Unsupported operand types for {label}: \'" + left.getClass().getSimpleName() + "\' and \'" + right.getClass().getSimpleName() + "\'");')
    print(f'        }}')
    print(f'    }}')
    print(f'    return result;')
    print('}')
    print()

for (op, name) in cmpops:
    print(f'@ModuleMethod("{name}")')
    print(f'public static PyObject py{name.capitalize()}(PyArguments args) {{')
    print(f'    return {name}(args.getArg(0), args.getArg(1));')
    print('}')
    print()
    print(f'public static PyObject {name}(PyObject left, PyObject right) {{')
    print(f'    PyObject result = left.__{name}__(right);')
    print(f'    if (result == PyNotImplemented.NotImplemented) {{')
    print(f'        throw new UnsupportedOperationException("Unsupported operand types for {op}: \'" + left.getClass().getSimpleName() + "\' and \'" + right.getClass().getSimpleName() + "\'");')
    print(f'    }}')
    print(f'    return result;')
    print('}')
    print()
