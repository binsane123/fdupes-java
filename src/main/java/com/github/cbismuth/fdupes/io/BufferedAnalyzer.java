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

import com.github.cbismuth.fdupes.cli.SystemPropertyGetter;
import com.github.cbismuth.fdupes.collect.PathComparator;
import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.github.cbismuth.fdupes.container.mutable.ByteBuffer;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.cbismuth.fdupes.container.mutable.MultimapCollector.toMultimap;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class BufferedAnalyzer {

    private static final Logger LOGGER = getLogger(BufferedAnalyzer.class);

    private final PathComparator pathComparator;
    private final SystemPropertyGetter systemPropertyGetter;

    public BufferedAnalyzer(final PathComparator pathComparator,
                            final SystemPropertyGetter systemPropertyGetter) {
        this.pathComparator = pathComparator;
        this.systemPropertyGetter = systemPropertyGetter;
    }

    public void analyze(final Collection<PathElement> input,
                        final Set<PathElement> uniqueElements,
                        final Multimap<PathElement, PathElement> duplicates) {
        input.parallelStream()
             .collect(toMultimap(PathElement::size))
             .asMap()
             .entrySet()
             .parallelStream()
             .map(Map.Entry::getValue)
             .forEach(values -> removeUniqueFiles(
                 values.parallelStream()
                       .map(pathElement -> new ByteBuffer(pathElement, systemPropertyGetter.getBufferSize()))
                       .collect(toList()),
                 uniqueElements,
                 duplicates
             ));

        reportDuplicationSize(duplicates);
    }

    private void removeUniqueFiles(final Collection<ByteBuffer> buffers,
                                   final Set<PathElement> uniqueElements,
                                   final Multimap<PathElement, PathElement> duplicates) {
        if (!buffers.isEmpty() && buffers.size() != 1) {
            buffers.forEach(ByteBuffer::read);

            if (buffers.iterator().next().getByteString().isEmpty()) {
                final List<PathElement> collect = buffers.parallelStream()
                                                         .peek(ByteBuffer::close)
                                                         .map(ByteBuffer::getPathElement)
                                                         .sorted(pathComparator)
                                                         .collect(toList());

                final PathElement original = collect.remove(0);

                uniqueElements.add(original);
                duplicates.putAll(original, collect);
            } else {
                final Collection<Collection<ByteBuffer>> values = buffers.parallelStream()
                                                                         .collect(toMultimap(ByteBuffer::getByteString))
                                                                         .asMap()
                                                                         .values();

                values.parallelStream()
                      .filter(collection -> collection.size() == 1)
                      .flatMap(Collection::stream)
                      .map(ByteBuffer::getPathElement)
                      .forEach(uniqueElements::add);

                values.parallelStream()
                      .filter(collection -> collection.size() > 1)
                      .forEach(collection -> removeUniqueFiles(collection, uniqueElements, duplicates));
            }
        }
    }

    private void reportDuplicationSize(final Multimap<PathElement, PathElement> duplicates) {
        final double sizeInMb = duplicates.asMap()
                                          .values()
                                          .parallelStream()
                                          .flatMap(Collection::parallelStream)
                                          .mapToLong(PathElement::size)
                                          .sum() / 1024.0 / 1024.0;

        LOGGER.info("Total size of duplicated files is {} mb", NumberFormat.getNumberInstance().format(sizeInMb));
    }

}
