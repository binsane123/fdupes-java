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

import com.codahale.metrics.Gauge;
import com.github.cbismuth.fdupes.collect.PathComparator;
import com.github.cbismuth.fdupes.immutable.PathElement;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.codahale.metrics.MetricRegistry.name;
import static com.github.cbismuth.fdupes.collect.MultimapCollector.toMultimap;
import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class BufferedAnalyzer {

    private static final Logger LOGGER = getLogger(BufferedAnalyzer.class);

    public Multimap<PathElement, PathElement> analyze(final Stream<PathElement> stream) {
        final Multimap<PathElement, PathElement> duplicates = synchronizedListMultimap(ArrayListMultimap.create());

        stream.collect(toMultimap(PathElement::size))
              .asMap()
              .entrySet()
              .parallelStream()
              .map(Map.Entry::getValue)
              .forEach(values -> removeUniqueFiles(
                  duplicates,
                  values.parallelStream()
                        .map(ByteBuffer::new)
                        .collect(toList())
              ));

        getMetricRegistry().register(name("collector", "bytes", "status", "finished"), (Gauge<Boolean>) () -> true);

        reportDuplicationSize(duplicates);

        return duplicates;
    }

    private void removeUniqueFiles(final Multimap<PathElement, PathElement> duplicates, final Collection<ByteBuffer> buffers) {
        if (!buffers.isEmpty() && buffers.size() != 1) {
            buffers.forEach(ByteBuffer::read);

            if (buffers.iterator().next().getByteString().isEmpty()) {
                final List<PathElement> collect = buffers.parallelStream()
                                                         .peek(ByteBuffer::close)
                                                         .map(ByteBuffer::getElement)
                                                         .sorted(PathComparator.INSTANCE)
                                                         .collect(toList());

                getMetricRegistry().counter(name("collector", "bytes", "counter", "total")).inc(collect.size());

                final PathElement original = collect.remove(0);

                getMetricRegistry().counter(name("collector", "bytes", "counter", "duplicates")).inc(collect.size());

                duplicates.putAll(original, collect);
            } else {
                buffers.parallelStream()
                       .collect(toMultimap(ByteBuffer::getByteString))
                       .asMap()
                       .values()
                       .parallelStream()
                       .filter(c -> c.size() > 1)
                       .forEach(c -> removeUniqueFiles(duplicates, c));
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
