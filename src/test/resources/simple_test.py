def test(name: 'str', *, sep: 'str' = '\n') -> None:
    print('Hello,', name, sep=sep)

test('Bob', sep=' ')
