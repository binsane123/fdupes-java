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
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import fdupes.io.DuplicateFileTreeWalker;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public final class Main {

    private static final Logger LOGGER = getLogger(Main.class);

    public static void main(final String... args) throws IOException {
        if (args.length == 0) {
            LOGGER.error("Usage: java -jar fdupes-<version>-all.jar <dir1> [<dir2>]...");
        } else {
            launch(args);
        }
    }

    private static void launch(final String[] args) throws IOException {
        final MetricRegistry metricRegistry = new MetricRegistry();

        try (final Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(metricRegistry).outputTo(getLogger("metrics")).build();
             final JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build()) {
            slf4jReporter.start(1L, MINUTES);
            jmxReporter.start();

            doIt(metricRegistry, args);

            slf4jReporter.report();
        }
    }

    private static void doIt(final MetricRegistry metricRegistry, final String[] args) throws IOException {
        final String workingDirectory = System.getProperty("user.dir");
        final String filename = "duplicates.log";

        final Path output = Paths.get(workingDirectory, filename);

        Files.write(output, fdupes(metricRegistry, args));

        LOGGER.info("Output log file located at [{}]", output);
    }

    private static Set<String> fdupes(final MetricRegistry metricRegistry, final String... rootPaths) {
        return new DuplicateFileTreeWalker(metricRegistry).extractDuplicates(asList(rootPaths));
    }

    private Main() {
        // NOT ALLOWED
    }

}
