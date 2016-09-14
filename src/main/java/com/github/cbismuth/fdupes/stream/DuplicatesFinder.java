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

import com.github.cbismuth.fdupes.io.BufferedAnalyzer;
import com.github.cbismuth.fdupes.io.PathEscapeFunction;
import com.github.cbismuth.fdupes.io.PathUtils;
import com.github.cbismuth.fdupes.md5.Md5Computer;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.codahale.metrics.MetricRegistry.name;
import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

public class DuplicatesFinder {

    private static final Logger LOGGER = getLogger(DuplicatesFinder.class);

    private final Md5Computer md5;
    private final StreamHandler handler = new StreamHandler();

    public DuplicatesFinder(final Md5Computer md5) {
        Preconditions.checkNotNull(md5, "null MD5 computer");

        this.md5 = md5;
    }

    public Set<String> extractDuplicates(final Collection<Path> elements) {
        Preconditions.checkNotNull(elements, "null file metadata collection");

        Stream<Path> stream = elements.parallelStream();

        final String passName1 = "size";
        LOGGER.info("Pass 1/3 - compare file by size ...");
        stream = handler.removeUniqueFilesByKey(stream, passName1, PathUtils::getPathSize);
        LOGGER.info("Pass 1/3 - compare file by size completed! - {} duplicate(s) found", getCount(passName1));

        final String passName2 = "md5";
        LOGGER.info("Pass 2/3 - compare file by MD5 ...");
        stream = handler.removeUniqueFilesByKey(stream, passName2, md5::compute);
        LOGGER.info("Pass 2/3 - compare file by MD5 completed! - {} duplicate(s) found", getCount(passName2));

        LOGGER.info("Pass 3/3 - compare file byte-by-byte ...");
        final BufferedAnalyzer analyzer = new BufferedAnalyzer(stream.collect(toList()));
        final Set<String> collect = analyzer.analyze()
                                            .asMap()
                                            .entrySet()
                                            .parallelStream()
                                            .map(Map.Entry::getValue)
                                            .flatMap(Collection::stream)
                                            .map(PathEscapeFunction.INSTANCE)
                                            .collect(toSet());
        LOGGER.info("Pass 3/3 - compare file byte-by-byte completed! - {} duplicate(s) found", collect.size());

        return collect;
    }

    private long getCount(final String name) {
        return getMetricRegistry().counter(name("multimap", name, "duplicates", "counter")).getCount();
    }

}
