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
import com.github.cbismuth.fdupes.immutable.PathElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.currentTimeMillis;

public class PathOrganizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathOrganizer.class);

    public void organize(final Iterable<PathElement> pathElements) throws IOException {
        final Path destination = Files.createDirectory(Paths.get(String.valueOf(currentTimeMillis())));
        final PathAnalyser pathAnalyser = new PathAnalyser();

        pathElements.forEach(pathElement -> pathAnalyser.getTimestampPath(destination, pathElement.getPath())
                                                        .ifPresent(path -> {
                                                            try {
                                                                Files.move(pathElement.getPath(), path);
                                                            } catch (final IOException e) {
                                                                LOGGER.error(e.getMessage(), e);
                                                            }
                                                        }));
    }

}
