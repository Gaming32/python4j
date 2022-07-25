"Generate the opcode fields for Opcode.java. This script should be run on Python 3.11 only."

import dis

for (key, value) in dis.opmap.items():
    print(f'public static final int {key} = {value};')

print()

for (i, op) in enumerate(['lt', 'le', 'eq', 'ne', 'ge', 'gt']):
    print(f'public static final int CMP_{op.upper()} = {i};')
