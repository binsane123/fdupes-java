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
import com.google.common.collect.Multimap;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

@Component
public class DuplicatesCsvReporter {

    public Path report(final Multimap<PathElement, PathElement> duplicates) throws IOException {
        final Path output = Paths.get(System.getProperty("user.dir"), "report.csv");

        try (CSVWriter writer = new CSVWriter(new FileWriter(output.toFile(), false))) {
            duplicates.asMap()
                      .entrySet()
                      .forEach(reportEntry(writer));
        }

        return output;
    }

    private Consumer<Map.Entry<PathElement, Collection<PathElement>>> reportEntry(final CSVWriter writer) {
        return e -> {
            final PathElement original = e.getKey();
            final Iterator<PathElement> iterator = e.getValue().iterator();

            reportOriginal(writer, iterator, original);
            reportDuplicates(writer, iterator);
        };
    }

    private void reportOriginal(final CSVWriter writer, final Iterator<PathElement> iterator, final PathElement original) {
        writer.writeNext(new String[] {
            original.getPath().toString(),
            iterator.next().getPath().toString()
        });
    }

    private void reportDuplicates(final CSVWriter writer, final Iterator<PathElement> iterator) {
        StreamSupport.stream(spliteratorUnknownSize(iterator, ORDERED), false)
                     .forEach(pathElement -> writer.writeNext(new String[] {
                         "",
                         pathElement.getPath().toString()
                     }));
    }

}
