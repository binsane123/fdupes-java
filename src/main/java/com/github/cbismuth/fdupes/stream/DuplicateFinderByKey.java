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

package com.github.cbismuth.fdupes.stream;

import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static com.github.cbismuth.fdupes.container.mutable.MultimapCollector.toMultimap;
import static java.util.stream.Collectors.toList;

@Component
public class DuplicateFinderByKey {

    public <K> Collection<PathElement> getDuplicates(final Collection<PathElement> input,
                                                     final Function<PathElement, K> keyMapper,
                                                     final Collection<PathElement> uniqueElements) {
        Preconditions.checkNotNull(input, "null pass stream");
        Preconditions.checkNotNull(keyMapper, "null pass key mapper");

        final Multimap<K, PathElement> multimap = input.parallelStream()
                                                       .collect(toMultimap(keyMapper));

        uniqueElements.addAll(
            multimap.asMap()
                    .entrySet()
                    .parallelStream()
                    .map(Map.Entry::getValue)
                    .filter(value -> value.size() == 1)
                    .flatMap(Collection::parallelStream)
                    .collect(toList())
        );

        return multimap.asMap()
                       .entrySet()
                       .parallelStream()
                       .map(Map.Entry::getValue)
                       .filter(value -> value.size() > 1)
                       .flatMap(Collection::parallelStream)
                       .collect(toList());
    }

}
