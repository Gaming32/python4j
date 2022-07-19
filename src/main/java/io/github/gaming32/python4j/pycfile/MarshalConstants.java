package io.github.gaming32.python4j.pycfile;

import io.github.gaming32.python4j.objects.PyObjectAccess;

public final class MarshalConstants {
    public static final int MAX_STACK_DEPTH = 1000;

    public static final int TYPE_NULL             = '0';
    public static final int TYPE_NONE             = 'N';
    public static final int TYPE_FALSE            = 'F';
    public static final int TYPE_TRUE             = 'T';
    public static final int TYPE_STOPITER         = 'S';
    public static final int TYPE_ELLIPSIS         = '.';
    public static final int TYPE_INT              = 'i';
    /**
     * TYPE_INT64 is not generated anymore.
     * Supported for backward compatibility only.
     */
    public static final int TYPE_INT64            = 'I';
    public static final int TYPE_FLOAT            = 'f';
    public static final int TYPE_BINARY_FLOAT     = 'g';
    public static final int TYPE_COMPLEX          = 'x';
    public static final int TYPE_BINARY_COMPLEX   = 'y';
    public static final int TYPE_LONG             = 'l';
    public static final int TYPE_STRING           = 's';
    public static final int TYPE_INTERNED         = 't';
    public static final int TYPE_REF              = 'r';
    public static final int TYPE_TUPLE            = '(';
    public static final int TYPE_LIST             = '[';
    public static final int TYPE_DICT             = '{';
    public static final int TYPE_CODE             = 'c';
    public static final int TYPE_UNICODE          = 'u';
    public static final int TYPE_UNKNOWN          = '?';
    public static final int TYPE_SET              = '<';
    public static final int TYPE_FROZENSET        = '>';
    public static final int FLAG_REF = 0x80;

    public static final int TYPE_ASCII            = 'a';
    public static final int TYPE_ASCII_INTERNED   = 'A';
    public static final int TYPE_SMALL_TUPLE      = ')';
    public static final int TYPE_SHORT_ASCII      = 'z';
    public static final int TYPE_SHORT_ASCII_INTERNED = 'Z';

    public static final int PyLong_MARSHAL_SHIFT = 15;
    public static final int PyLong_MARSHAL_BASE = 1 << PyLong_MARSHAL_SHIFT;
    public static final int PyLong_MARSHAL_MASK = PyLong_MARSHAL_BASE - 1;
    public static final int PyLong_MARSHAL_RATIO = PyObjectAccess.PyLong_SHIFT / PyLong_MARSHAL_SHIFT;

    public MarshalConstants() {
    }
}
