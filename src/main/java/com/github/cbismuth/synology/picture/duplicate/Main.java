package com.github.cbismuth.synology.picture.duplicate;

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
import static org.slf4j.LoggerFactory.getLogger;

final class Main {

    private static final Logger LOGGER = getLogger(Main.class);

    public static void main(final String... args) throws IOException {
        final String workingDirectory = System.getProperty("user.dir");
        final String filename = format("duplicates-%d.log", currentTimeMillis());

        final Path output = Paths.get(workingDirectory, filename);

        Files.write(output, fdups(args), CREATE_NEW);

        LOGGER.info("Output log file located at [{}]", output);
    }

    private static Set<String> fdups(final String... rootPaths) {
        return new DuplicateFileTreeWalker().extractDuplicates(asList(rootPaths));
    }

}
