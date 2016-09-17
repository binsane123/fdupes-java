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

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.cbismuth.fdupes.collect.MultimapCollector.toMultimap;

public class StreamHandler {

    public <K> Stream<Path> removeUniqueFilesByKey(final Stream<Path> stream, final String name, final Function<Path, K> keyMapper) {
        return removeUniqueFilesByKey(stream, name, keyMapper, false);
    }

    private <K> Stream<Path> removeUniqueFilesByKey(final Stream<Path> stream, final String name, final Function<Path, K> keyMapper, final boolean removeOriginals) {
        Preconditions.checkNotNull(stream, "null pass stream");
        Preconditions.checkNotNull(name, "null pass name");
        Preconditions.checkNotNull(keyMapper, "null pass key mapper");

        final Multimap<K, Path> multimap = stream.collect(toMultimap(name, keyMapper));

        final Stream<Map.Entry<K, Collection<Path>>> entryWithDuplicates = multimap.asMap()
                                                                                   .entrySet()
                                                                                   .parallelStream()
                                                                                   .filter(e -> e.getValue().size() > 1);

        return entryWithDuplicates.flatMap(e -> e.getValue().parallelStream());
    }

}
