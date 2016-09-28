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

package com.github.cbismuth.fdupes.report;

import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.github.cbismuth.fdupes.io.PathEscapeFunction;
import com.google.common.collect.Multimap;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

@Component
public class DuplicatesLogReporter {

    private final PathEscapeFunction pathEscapeFunction;

    public DuplicatesLogReporter(final PathEscapeFunction pathEscapeFunction) {
        this.pathEscapeFunction = pathEscapeFunction;
    }

    public Path report(final Multimap<PathElement, PathElement> duplicates) throws IOException {
        final Path output = Paths.get(System.getProperty("user.dir"), "duplicates.log");

        final String content = duplicates.asMap()
                                         .entrySet()
                                         .stream()
                                         .map(Map.Entry::getValue)
                                         .flatMap(Collection::stream)
                                         .map(PathElement::getPath)
                                         .map(Path::toString)
                                         .map(pathEscapeFunction)
                                         .collect(joining(System.getProperty("line.separator")));

        Files.write(output, content.getBytes(UTF_8));

        return output;
    }

}
