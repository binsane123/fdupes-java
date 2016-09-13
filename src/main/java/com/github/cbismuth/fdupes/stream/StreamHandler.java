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

import com.github.cbismuth.fdupes.immutable.FileMetadata;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.codahale.metrics.MetricRegistry.name;
import static com.github.cbismuth.fdupes.collect.MultimapCollector.toMultimap;

public class StreamHandler {

    public <K> Stream<FileMetadata> removeUniqueFilesByKey(final Stream<FileMetadata> stream, final String name, final Function<FileMetadata, K> keyMapper) {
        Preconditions.checkNotNull(stream, "null pass stream");
        Preconditions.checkNotNull(name, "null pass name");
        Preconditions.checkNotNull(keyMapper, "null pass key mapper");

        final Multimap<K, FileMetadata> multimap = stream.collect(toMultimap(name("multimap", name), keyMapper));

        final Stream<Map.Entry<K, Collection<FileMetadata>>> entryWithDuplicates = multimap.asMap()
                                                                                           .entrySet()
                                                                                           .parallelStream()
                                                                                           .filter(e -> e.getValue().size() > 1);

        return entryWithDuplicates.flatMap(e -> e.getValue().parallelStream());
    }

}
