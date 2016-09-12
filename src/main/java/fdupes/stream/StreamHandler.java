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

package fdupes.stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import fdupes.collect.RemoveOriginalFromEntryFunction;
import fdupes.immutable.FileMetadata;
import fdupes.io.PathEscapeFunction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codahale.metrics.MetricRegistry.name;
import static fdupes.collect.MultimapCollector.toMultimap;

public class StreamHandler {

    public <K> Stream<FileMetadata> removeUniqueFilesByKey(final Stream<FileMetadata> stream, final String name, final Function<FileMetadata, K> keyMapper) {
        return removeUniqueFilesByKey(stream, name, keyMapper, false);
    }

    public <K> Stream<FileMetadata> removeUniqueFilesByKeyAndOriginals(final Stream<FileMetadata> stream, final String name, final Function<FileMetadata, K> keyMapper) {
        return removeUniqueFilesByKey(stream, name, keyMapper, true);
    }

    private <K> Stream<FileMetadata> removeUniqueFilesByKey(final Stream<FileMetadata> stream, final String name, final Function<FileMetadata, K> keyMapper, final boolean removeOriginals) {
        Preconditions.checkNotNull(stream, "null pass stream");
        Preconditions.checkNotNull(name, "null pass name");
        Preconditions.checkNotNull(keyMapper, "null pass key mapper");

        final Multimap<K, FileMetadata> multimap = stream.collect(toMultimap(name("multimap", name), keyMapper));

        final Stream<Map.Entry<K, Collection<FileMetadata>>> entryWithDuplicates = multimap.asMap()
                                                                                           .entrySet()
                                                                                           .stream()
                                                                                           .filter(e -> e.getValue().size() > 1);

        final Stream<FileMetadata> result;
        if (!removeOriginals) {
            result = entryWithDuplicates.flatMap(e -> e.getValue().stream());
        } else {
            result = entryWithDuplicates.flatMap(new RemoveOriginalFromEntryFunction<>());
        }

        return result;
    }

    public Set<String> extractAbsolutePaths(final Stream<FileMetadata> stream) {
        Preconditions.checkNotNull(stream, "null stream");

        return stream.map(FileMetadata::getAbsolutePath)
                     .map(PathEscapeFunction.INSTANCE)
                     .collect(Collectors.toCollection(TreeSet::new));
    }

}
