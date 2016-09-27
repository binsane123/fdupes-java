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

package com.github.cbismuth.fdupes.collect;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

public class PathAnalyser {

    private static final Logger LOGGER = getLogger(PathAnalyser.class);

    private static final Pattern PATTERN = Pattern.compile("([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2})([0-9]{2}).*\\.(.*)$");

    public Optional<Path> getTimestampPath(final Path destination, final Path path) {
        final Optional<Path> result;

        final Matcher matcher = PATTERN.matcher(FilenameUtils.getName(path.toString()));
        if (matcher.matches()) {
            result = Optional.of(onMatch(destination, path, matcher));
        } else {
            LOGGER.warn("File [{}] doesn't match pattern", path);

            result = Optional.empty();
        }

        return result;
    }

    private Path onMatch(final Path destination, final Path path, final Matcher matcher) {
        LOGGER.debug("Pattern matched with [{}]", path);

        final String year = matcher.group(1);
        final String month = matcher.group(2);
        final String day = matcher.group(3);
        final String hour = matcher.group(4);
        final String minute = matcher.group(5);
        final String second = matcher.group(6);
        final String extension = matcher.group(7);

        Path newPath = Paths.get(destination.toString(),
                                 year, month, day,
                                 new StringBuilder().append(year)
                                                    .append(month)
                                                    .append(day)
                                                    .append(hour)
                                                    .append(minute)
                                                    .append(second)
                                                    .append('.')
                                                    .append(extension)
                                                    .toString());

        int i = 1;
        while (Files.exists(newPath)) {
            newPath = Paths.get(destination.toString(),
                                year, month, day,
                                new StringBuilder().append(year)
                                                   .append(month)
                                                   .append(day)
                                                   .append(hour)
                                                   .append(minute)
                                                   .append(second)
                                                   .append('-')
                                                   .append(i++)
                                                   .append('.')
                                                   .append(extension)
                                                   .toString());
        }

        return newPath;
    }

}
