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

import com.codahale.metrics.Timer;
import com.github.cbismuth.fdupes.collect.FilenamePredicate;
import com.github.cbismuth.fdupes.immutable.PathElement;
import com.github.cbismuth.fdupes.md5.Md5Computer;
import com.github.cbismuth.fdupes.stream.DuplicatesFinder;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import static com.codahale.metrics.MetricRegistry.name;
import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.slf4j.LoggerFactory.getLogger;

public class DirectoryWalker {

    private static final Logger LOGGER = getLogger(DirectoryWalker.class);

    private final DuplicatesFinder duplicatesFinder;

    private final Set<PathElement> paths = newConcurrentHashSet();
    private final Set<Path> pathsInError = newConcurrentHashSet();

    public DirectoryWalker(final Md5Computer md5Computer) {
        Preconditions.checkNotNull(md5Computer, "null MD5 computer");

        duplicatesFinder = new DuplicatesFinder(md5Computer);
    }

    public Set<String> extractDuplicates(final Iterable<String> inputPaths) throws IOException {
        Preconditions.checkNotNull(inputPaths, "null input path collection");

        paths.clear();

        inputPaths.forEach(rootPath -> {
            final Path path = Paths.get(rootPath);

            if (FilenamePredicate.INSTANCE.accept(path)) {
                if (Files.isDirectory(path)) {
                    handleDirectory(path);
                } else if (Files.isRegularFile(path)) {
                    handleRegularFile(path);
                } else {
                    LOGGER.warn("[{}] is not a directory or a regular file", rootPath);
                }
            }
        });

        reportPathsInError();

        return duplicatesFinder.extractDuplicates(paths);
    }

    private void handleDirectory(final Path path) {
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path, FilenamePredicate.INSTANCE)) {
            stream.forEach(p -> {
                if (Files.isDirectory(p)) {
                    getMetricRegistry().counter(name("fs", "counter", "directories")).inc();

                    handleDirectory(p);
                } else {
                    handleRegularFile(p);
                }
            });
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void handleRegularFile(final Path path) {
        try {
            try (final Timer.Context ignored = getMetricRegistry().timer(name("fs", "timer", "files", "attributes", "read")).time()) {
                paths.add(new PathElement(path, Files.readAttributes(path, BasicFileAttributes.class)));
            }

            getMetricRegistry().counter(name("fs", "counter", "files", "ok")).inc();
        } catch (final IOException ignored) {
            pathsInError.add(path);

            getMetricRegistry().counter(name("fs", "counter", "files", "ko")).inc();
        }
    }

    private void reportPathsInError() throws IOException {
        final Path output = Paths.get(System.getProperty("user.dir"), "errors.log");
        final String content = pathsInError.parallelStream()
                                           .map(Path::toString)
                                           .map(PathEscapeFunction.INSTANCE)
                                           .collect(joining(System.getProperty("line.separator")));

        Files.write(output, content.getBytes(UTF_8));
    }

}
