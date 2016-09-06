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

package fdupes.container;

import fdupes.collect.MultimapCollector;
import fdupes.io.PathEscapeFunction;
import fdupes.md5.Md5SumHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;

public class DuplicatesFinder {

    private final Md5SumHelper md5SumHelper;

    public DuplicatesFinder(final Md5SumHelper md5SumHelper) {
        this.md5SumHelper = md5SumHelper;
    }

    public Set<String> extractDuplicates(final Collection<FileMetadata> elements) {
        return elements.stream()
                       // index file metadata elements by file size
                       .collect(MultimapCollector.toMultimap(name("multimap", "by-size"), FileMetadata::getSize))

                       // get a stream of entries
                       .asMap().entrySet().stream()

                       // keep duplicates by size only
                       .filter(e -> e.getValue().size() > 1)

                       // flatten all duplicates by size
                       .flatMap(e -> e.getValue().stream())

                       // index file metadata elements by md5sum
                       .collect(MultimapCollector.toMultimap(name("multimap", "by-md5sum"), md5SumHelper::md5sum))

                       // get a stream of entries
                       .asMap().entrySet().stream()

                       // keep duplicates by md5sum only
                       .filter(e -> e.getValue().size() > 1)

                       // remove first element of each collection (i.e. original file)
                       .peek(e -> sortAndRemoveFirst((List<FileMetadata>) e.getValue()))

                       // flatten all duplicates by md5sum (i.e. all identified duplicates to remove)
                       .flatMap(e -> e.getValue().stream())

                       // extract absolute paths only
                       .map(FileMetadata::getAbsolutePath)

                       // normalize absolute paths
                       .map(PathEscapeFunction.INSTANCE)

                       // collect absolute paths
                       .collect(Collectors.toCollection(TreeSet::new));
    }

    private void sortAndRemoveFirst(final List<FileMetadata> value) {
        Collections.sort(value, FileMetadataComparator.INSTANCE);
        value.remove(0);
    }

}
