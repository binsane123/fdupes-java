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

package com.github.cbismuth.fdupes.io;

import com.github.cbismuth.fdupes.collect.FileMetadataComparator;
import com.github.cbismuth.fdupes.immutable.FileMetadata;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.cbismuth.fdupes.collect.MultimapCollector.toMultimap;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static com.google.common.collect.Multimaps.unmodifiableMultimap;
import static java.util.stream.Collectors.toList;

public class BufferedByteAnalyzer {

    private final Collection<FileMetadata> input = newArrayList();

    private final Multimap<String, String> duplicates = synchronizedListMultimap(ArrayListMultimap.create());

    public BufferedByteAnalyzer(final Collection<FileMetadata> input) {
        Preconditions.checkNotNull(input, "null file metadata collection");

        this.input.addAll(input);
    }

    public Multimap<String, String> analyze() {
        input.parallelStream()
             .collect(toMultimap(FileMetadata::getSize))
             .asMap()
             .entrySet()
             .parallelStream()
             .map(Map.Entry::getValue)
             .forEach(values -> removeUniqueFiles(
                 values.parallelStream()
                       .map(ByteBuffer::new)
                       .collect(toList())
             ));

        return unmodifiableMultimap(duplicates);
    }

    private void removeUniqueFiles(final Collection<ByteBuffer> buffers) {
        if (!buffers.isEmpty() && buffers.size() != 1) {
            buffers.forEach(ByteBuffer::read);

            if (buffers.iterator().next().getByteString().isEmpty()) {
                final List<String> collect = buffers.parallelStream()
                                                    .map(ByteBuffer::getFileMetadata)
                                                    .sorted(FileMetadataComparator.INSTANCE)
                                                    .map(FileMetadata::getAbsolutePath)
                                                    .collect(toList());

                final String original = collect.remove(0);

                duplicates.putAll(original, collect);
            } else {
                buffers.parallelStream()
                       .collect(toMultimap(ByteBuffer::getByteString))
                       .asMap()
                       .entrySet()
                       .parallelStream()
                       .forEach(e -> {
                           if (e.getValue().size() == 1) {
                               input.remove(e.getValue().iterator().next().getFileMetadata());
                           } else {
                               removeUniqueFiles(e.getValue());
                           }
                       });
            }
        }
    }

}
