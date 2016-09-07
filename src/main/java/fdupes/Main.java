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

package fdupes;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Slf4jReporter;
import fdupes.io.DirectoryWalker;
import fdupes.io.DuplicatesWriter;
import fdupes.md5.Md5Computer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public final class Main {

    private static final Logger LOGGER = getLogger(Main.class);

    public static void main(final String... args) throws IOException {
        if (args.length == 0) {
            System.out.println(version());
            System.err.println("Usage: java -jar fdupes-<version>-all.jar <dir1> [<dir2>]...");
        } else if (args.length == 1 && ("-v".equals(args[0]) || "--version".equals(args[0]))) {
            System.out.println(version());
        } else {
            launch(args);
        }
    }

    private static String version() {
        return String.format("fdupes-java version %s", Main.class.getPackage().getImplementationVersion());
    }

    private static void launch(final String[] args) throws IOException {
        try (final Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(getMetricRegistry()).outputTo(getLogger("metrics")).build();
             final JmxReporter jmxReporter = JmxReporter.forRegistry(getMetricRegistry()).build()) {
            slf4jReporter.start(1L, MINUTES);
            jmxReporter.start();

            doIt(args);

            slf4jReporter.report();
        }
    }

    private static void doIt(final String[] args) throws IOException {
        final DirectoryWalker walker = new DirectoryWalker(new Md5Computer());
        final DuplicatesWriter writer = new DuplicatesWriter();

        final List<String> inputPaths = asList(args);
        final Set<String> absolutePathsOfDuplicates = walker.extractDuplicates(inputPaths);
        final Path outputPath = writer.write(absolutePathsOfDuplicates);

        LOGGER.info("Output file written at [{}]", outputPath);
    }

    private Main() {
        // PRIVATE
    }

}
