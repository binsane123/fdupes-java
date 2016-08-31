package com.github.cbismuth.synology.picture.duplicate;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

final class Main {

    private static final Logger LOGGER = getLogger(Main.class);

    public static void main(final String... args) throws IOException {
        final MetricRegistry metricRegistry = new MetricRegistry();

        try (final Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(metricRegistry).outputTo(getLogger("metrics")).build();
             final JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build()) {
            slf4jReporter.start(1, MINUTES);
            jmxReporter.start();

            doIt(metricRegistry, args);
        }
    }

    private static void doIt(final MetricRegistry metricRegistry, final String[] args) throws IOException {
        final String workingDirectory = System.getProperty("user.dir");
        final String filename = format("duplicates-%d.log", currentTimeMillis());

        final Path output = Paths.get(workingDirectory, filename);

        Files.write(output, fdups(metricRegistry, args), CREATE_NEW);

        LOGGER.info("Output log file located at [{}]", output);
    }

    private static Set<String> fdups(final MetricRegistry metricRegistry, final String... rootPaths) {
        return new DuplicateFileTreeWalker(metricRegistry).extractDuplicates(asList(rootPaths));
    }

}
