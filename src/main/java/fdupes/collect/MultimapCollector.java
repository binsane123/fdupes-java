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

package fdupes.collect;

import com.codahale.metrics.Gauge;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.codahale.metrics.MetricRegistry.name;
import static fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static java.util.stream.Collector.Characteristics.UNORDERED;

public final class MultimapCollector<T, K, V> implements Collector<T, Multimap<K, V>, Multimap<K, V>> {

    private final String name;

    private final Function<T, K> keyGetter;
    private final Function<T, V> valueGetter;

    private MultimapCollector(final String name,
                              final Function<T, K> keyGetter,
                              final Function<T, V> valueGetter) {
        this.name = name;

        this.keyGetter = keyGetter;
        this.valueGetter = valueGetter;
    }

    public static <T, K, V> MultimapCollector<T, K, T> toMultimap(final String name, final Function<T, K> keyGetter) {
        return toMultimap(name, keyGetter, v -> v);
    }

    public static <T, K, V> MultimapCollector<T, K, V> toMultimap(final String name, final Function<T, K> keyGetter, final Function<T, V> valueGetter) {
        return new MultimapCollector<>(name, keyGetter, valueGetter);
    }

    @Override
    public Supplier<Multimap<K, V>> supplier() {
        return ArrayListMultimap::create;
    }

    @Override
    public BiConsumer<Multimap<K, V>, T> accumulator() {
        return (map, element) -> {
            getMetricRegistry().counter(name(name, "total", "counter")).inc();

            final K key = keyGetter.apply(element);
            final V value = valueGetter.apply(element);

            if (map.containsKey(key)) {
                getMetricRegistry().counter(name(name, "duplicates", "counter")).inc();
            }

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
        getMetricRegistry().register(name(name, "finished"), (Gauge<Boolean>) () -> true);

        return map -> map;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return ImmutableSet.of(UNORDERED);
    }

}
