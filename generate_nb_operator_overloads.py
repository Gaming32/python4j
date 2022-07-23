from opcode import _nb_ops  # type: ignore

opnames = (
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
)


print('// FOR: PyObject.java')

for name in opnames:
    for subname in ('', 'r'):
        print(f'public PyObject __{subname}{name}__(PyObject other) {{')
        print(f'    return PyNotImplemented.NotImplemented;')
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

for (i, name) in enumerate(opnames):
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
    print(f'            throw new UnsupportedOperationException("Unsupported operand types for {_nb_ops[i][1]}: \'" + left.getClass().getSimpleName() + "\' and \'" + right.getClass().getSimpleName() + "\'");')
    print(f'        }}')
    print(f'    }}')
    print(f'    return result;')
    print('}')
    print()
