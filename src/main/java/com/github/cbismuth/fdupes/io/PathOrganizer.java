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

import com.github.cbismuth.fdupes.collect.PathAnalyser;
import com.github.cbismuth.fdupes.container.immutable.PathElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PathOrganizer {

    private static final Logger LOGGER = getLogger(PathOrganizer.class);

    private final PathAnalyser pathAnalyser;

    public PathOrganizer(final PathAnalyser pathAnalyser) {
        this.pathAnalyser = pathAnalyser;
    }

    public void organize(final Iterable<PathElement> uniqueElements) throws IOException {
        organize(System.getProperty("user.dir"), String.valueOf(currentTimeMillis()), uniqueElements);
    }

    public void organize(final String workingDirectory,
                         final String subDirectoryName,
                         final Iterable<PathElement> uniqueElements) throws IOException {
        final Path directoryToCreate = Paths.get(workingDirectory, subDirectoryName);
        final Path destination = Files.createDirectory(directoryToCreate);

        moveUniqueFiles(destination, uniqueElements);
    }

    private void moveUniqueFiles(final Path destination,
                                 final Iterable<PathElement> uniqueElements) {
        final AtomicInteger counter = new AtomicInteger(1);

        uniqueElements.forEach(pathElement -> {
            final Optional<Path> timestampPath = pathAnalyser.getTimestampPath(destination, pathElement.getPath());

            if (timestampPath.isPresent()) {
                onTimestampPath(pathElement, timestampPath.get());
            } else {
                onNoTimestampPath(destination, pathElement, counter);
            }
        });
    }

    private void onTimestampPath(final PathElement pathElement, final Path timestampPath) {
        try {
            FileUtils.moveFile(
                pathElement.getPath().toFile(),
                timestampPath.toFile()
            );
        } catch (final IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void onNoTimestampPath(final Path destination,
                                   final PathElement pathElement,
                                   final AtomicInteger counter) {
        final Path path = pathElement.getPath();

        final String baseName = FilenameUtils.getBaseName(path.toString());
        final int count = counter.getAndIncrement();
        final String extension = FilenameUtils.getExtension(path.toString());

        final String newName = String.format("%s-%d.%s", baseName, count, extension);

        final Path sibling = path.resolveSibling(newName);

        try {
            FileUtils.moveFile(
                path.toFile(),
                sibling.toFile()
            );

            FileUtils.moveFileToDirectory(
                sibling.toFile(),
                Paths.get(destination.toString(), "misc").toFile(),
                true
            );
        } catch (final IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

}
