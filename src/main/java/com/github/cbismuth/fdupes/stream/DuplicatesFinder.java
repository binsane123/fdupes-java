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

import com.github.cbismuth.fdupes.immutable.PathElement;
import com.github.cbismuth.fdupes.io.BufferedAnalyzer;
import com.github.cbismuth.fdupes.md5.Md5Computer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import static com.codahale.metrics.MetricRegistry.name;
import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

public class DuplicatesFinder {

    private static final Logger LOGGER = getLogger(DuplicatesFinder.class);

    private final Md5Computer md5Computer;
    private final StreamHandler handler = new StreamHandler();

    public DuplicatesFinder(final Md5Computer md5Computer) {
        Preconditions.checkNotNull(md5Computer, "null MD5 computer");

        this.md5Computer = md5Computer;
    }

    public Multimap<PathElement, PathElement> extractDuplicates(final Collection<PathElement> elements) throws IOException {
        Preconditions.checkNotNull(elements, "null file metadata collection");

        Stream<PathElement> stream = elements.parallelStream();

        LOGGER.info("Pass 1/3 - compare file by size ...");
        final String passName1 = "size";
        stream = handler.removeUniqueFilesByKey(stream, passName1, PathElement::size);
        LOGGER.info("Pass 1/3 - compare file by size completed! - {} duplicate(s) found", getCount(passName1));

        LOGGER.info("Pass 2/3 - compare file by MD5 ...");
        final String passName2 = "md5";
        stream = handler.removeUniqueFilesByKey(stream, passName2, md5Computer::compute);
        LOGGER.info("Pass 2/3 - compare file by MD5 completed! - {} duplicate(s) found", getCount(passName2));

        LOGGER.info("Pass 3/3 - compare file byte-by-byte ...");
        final BufferedAnalyzer analyzer = new BufferedAnalyzer();
        final Multimap<PathElement, PathElement> duplicates = analyzer.analyze(stream);
        LOGGER.info("Pass 3/3 - compare file byte-by-byte completed! - {} duplicate(s) found", duplicates.asMap()
                                                                                                         .entrySet()
                                                                                                         .parallelStream()
                                                                                                         .map(Map.Entry::getValue)
                                                                                                         .flatMap(Collection::stream)
                                                                                                         .collect(toSet())
                                                                                                         .size());

        return duplicates;
    }

    private long getCount(final String name) {
        return getMetricRegistry().counter(name("collector", name, "counter", "duplicates")).getCount();
    }

}
