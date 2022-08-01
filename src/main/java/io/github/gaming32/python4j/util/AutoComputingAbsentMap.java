package io.github.gaming32.python4j.util;

import java.util.Map;
import java.util.function.Function;

public final class AutoComputingAbsentMap<K, V> extends AbstractForwardingMap<K, V> {
    private final Function<? extends K, ? extends V> compute;

    public AutoComputingAbsentMap(Map<K, V> map, Function<K, V> compute) {
        super(map);
        this.compute = compute;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public V get(Object key) {
        return (V)map.computeIfAbsent((K)key, (Function)compute);
    }
}
