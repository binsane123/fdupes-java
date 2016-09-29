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

package com.github.cbismuth.fdupes;

import com.github.cbismuth.fdupes.cli.SystemPropertyGetter;
import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.github.cbismuth.fdupes.io.DirectoryWalker;
import com.github.cbismuth.fdupes.io.PathEscapeFunction;
import com.github.cbismuth.fdupes.io.PathOrganizer;
import com.github.cbismuth.fdupes.report.DuplicatesCsvReporter;
import com.github.cbismuth.fdupes.report.DuplicatesLogReporter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class Launcher {

    private static final Logger LOGGER = getLogger(Launcher.class);

    private final PathOrganizer pathOrganizer;
    private final DirectoryWalker directoryWalker;
    private final PathEscapeFunction pathEscapeFunction;
    private final SystemPropertyGetter systemPropertyGetter;

    public Launcher(final PathOrganizer pathOrganizer,
                    final DirectoryWalker directoryWalker,
                    final PathEscapeFunction pathEscapeFunction,
                    final SystemPropertyGetter systemPropertyGetter) {
        this.pathOrganizer = pathOrganizer;
        this.pathEscapeFunction = pathEscapeFunction;
        this.directoryWalker = directoryWalker;
        this.systemPropertyGetter = systemPropertyGetter;
    }

    public Path launch(final Collection<String> args) throws IOException {
        try {
            final Set<PathElement> uniqueElements = newConcurrentHashSet();
            final Multimap<PathElement, PathElement> duplicates = synchronizedListMultimap(ArrayListMultimap.create());

            directoryWalker.extractDuplicates(args, uniqueElements, duplicates);

            if (systemPropertyGetter.doOrganize()) {
                pathOrganizer.organize(uniqueElements);
            }

            final Path csvReport = new DuplicatesCsvReporter().report(duplicates);
            LOGGER.info("CSV report created at [{}]", csvReport);

            final Path logReport = new DuplicatesLogReporter(pathEscapeFunction).report(duplicates);
            LOGGER.info("Log report created at [{}]", csvReport);

            return logReport;
        } catch (final OutOfMemoryError ignored) {
            LOGGER.error("Not enough memory, solutions are:");
            LOGGER.error("\t- increase Java heap size (e.g. -Xmx512m),");
            LOGGER.error("\t- decrease byte buffer size (e.g. -Dfdupes.buffer.size=8k - default is 64k),");
            LOGGER.error("\t- reduce the level of parallelism (e.g. -Dfdupes.parallelism=1).");

            return null;
        }
    }

}
