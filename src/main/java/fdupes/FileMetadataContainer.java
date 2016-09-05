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

package fdupes;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.slf4j.LoggerFactory.getLogger;

class FileMetadataContainer {

    private static final Logger LOGGER = getLogger(FileMetadataContainer.class);

    private final MetricRegistry metricRegistry;
    private final Collection<FileMetadata> fileMetadataCollection = newHashSet();
    private final Md5SumHelper md5SumHelper;

    FileMetadataContainer(final MetricRegistry metricRegistry) {
        this(metricRegistry, new Md5SumHelper());
    }

    FileMetadataContainer(final MetricRegistry metricRegistry, final Md5SumHelper md5SumHelper) {
        this.metricRegistry = metricRegistry;
        this.md5SumHelper = md5SumHelper;
    }

    void clear() {
        fileMetadataCollection.clear();
    }

    void addFile(final Path path) {
        try {
            fileMetadataCollection.add(new FileMetadata(path.toString(), Files.size(path)));
        } catch (final Exception e) {
            LOGGER.error("Can't read file [{}] ({})", path, e.getClass().getSimpleName());
        }
    }

    Set<String> extractDuplicates() {
        return fileMetadataCollection.stream()
                                     // index file metadata elements by file size
                                     .collect(MultimapCollector.toMultimap(metricRegistry, "duplicatesBySize", FileMetadata::getSize))
                                     // get a stream of entries
                                     .asMap()
                                     .entrySet()
                                     .stream()
                                     // keep duplicates by size only
                                     .filter(e -> e.getValue().size() > 1)
                                     // flatten all duplicates by size
                                     .flatMap(e -> e.getValue().stream())
                                     .peek(e -> LOGGER.debug("Duplicate by size detected at [{}]", e.getAbsolutePath()))
                                     // index file metadata elements by md5sum
                                     .collect(MultimapCollector.toMultimap(metricRegistry, "duplicatesByMd5Sum", this::md5sum))
                                     // get a stream of entries
                                     .asMap()
                                     .entrySet()
                                     .stream()
                                     // keep duplicates by md5sum only
                                     .filter(e -> e.getValue().size() > 1)
                                     // remove first element of each collection (i.e. original file)
                                     .peek(e -> sortAndRemoveFirst((List<FileMetadata>) e.getValue()))
                                     // flatten all duplicates by md5sum (i.e. all identified duplicates to remove)
                                     .flatMap(e -> e.getValue().stream())
                                     .peek(e -> LOGGER.debug("Duplicate by md5sum detected at [{}]", e.getAbsolutePath()))
                                     // extract absolute paths only
                                     .map(e -> format("\"%s\"", e.getAbsolutePath()))
                                     // collect absolute paths
                                     .collect(Collectors.toCollection(TreeSet::new));
    }

    private String md5sum(final FileMetadata fileMetadata) {
        try (final Timer.Context ignored = metricRegistry.timer(name("md5sum", "timer")).time()) {
            return md5SumHelper.md5sum(fileMetadata);
        } catch (final Exception e) {
            LOGGER.error("Can't compute md5sum from file [{}] ({})", fileMetadata.getAbsolutePath(), e.getClass().getSimpleName());
            return randomUUID().toString();
        }
    }

    private void sortAndRemoveFirst(final List<FileMetadata> value) {
        Collections.sort(value, (o1, o2) -> o1.getAbsolutePath().compareTo(o2.getAbsolutePath()));
        value.remove(0);
    }

}
