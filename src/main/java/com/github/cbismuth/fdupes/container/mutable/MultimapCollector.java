/*
 * The MIT License (MIT)
 * Copyright (c) 2016 Christophe Bismuth
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.cbismuth.fdupes.container.mutable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static java.util.stream.Collector.Characteristics.UNORDERED;

public final class MultimapCollector<T, K, V> implements Collector<T, Multimap<K, V>, Multimap<K, V>> {

    private final Function<T, K> keyGetter;
    private final Function<T, V> valueGetter;

    private MultimapCollector(final Function<T, K> keyGetter,
                              final Function<T, V> valueGetter) {
        this.keyGetter = keyGetter;
        this.valueGetter = valueGetter;
    }

    public static <T, K, V> MultimapCollector<T, K, T> toMultimap(final Function<T, K> keyGetter) {
        Preconditions.checkNotNull(keyGetter, "null multimap key getter");

        return toMultimap(keyGetter, v -> v);
    }

    public static <T, K, V> MultimapCollector<T, K, V> toMultimap(final Function<T, K> keyGetter, final Function<T, V> valueGetter) {
        Preconditions.checkNotNull(keyGetter, "null multimap key getter");
        Preconditions.checkNotNull(valueGetter, "null multimap value getter");

        return new MultimapCollector<>(keyGetter, valueGetter);
    }

    @Override
    public Supplier<Multimap<K, V>> supplier() {
        return () -> synchronizedListMultimap(ArrayListMultimap.create());
    }

    @Override
    public BiConsumer<Multimap<K, V>, T> accumulator() {
        return (map, element) -> {
            final K key = keyGetter.apply(element);
            final V value = valueGetter.apply(element);

            map.put(key, value);
        };
    }

    @Override
    public BinaryOperator<Multimap<K, V>> combiner() {
        return (map1, map2) -> {
            map1.putAll(map2);
            return map1;
        };
    }

    @Override
    public Function<Multimap<K, V>, Multimap<K, V>> finisher() {
        return map -> map;
    }

    @Override
    public Set<Collector.Characteristics> characteristics() {
        return ImmutableSet.of(UNORDERED);
    }

}
