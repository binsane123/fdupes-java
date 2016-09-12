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

package com.github.cbismuth.fdupes.collect;

import com.github.cbismuth.fdupes.immutable.FileMetadata;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class RemoveOriginalFromEntryFunction<K> implements Function<Map.Entry<K, Collection<FileMetadata>>, Stream<? extends FileMetadata>> {

    private final FileMetadataComparator comparator = FileMetadataComparator.INSTANCE;

    @Override
    public Stream<? extends FileMetadata> apply(final Map.Entry<K, Collection<FileMetadata>> entry) {
        Preconditions.checkNotNull(entry, "null file metadata entry");

        final List<FileMetadata> sorted = entry.getValue()
                                               .parallelStream()
                                               .sorted(comparator)
                                               .collect(toList());

        sorted.remove(0);

        return sorted.parallelStream();
    }

}
