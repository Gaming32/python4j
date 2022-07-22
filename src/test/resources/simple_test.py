def a() -> 'str':
    def b() -> 'str':
        return 'hi'
    return b()

print(a())
