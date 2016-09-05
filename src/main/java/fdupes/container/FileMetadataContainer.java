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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import fdupes.collect.MultimapCollector;
import fdupes.md5.Md5SumHelper;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.UUID.randomUUID;
import static org.slf4j.LoggerFactory.getLogger;

public class FileMetadataContainer {

    private static final Logger LOGGER = getLogger(FileMetadataContainer.class);

    private final MetricRegistry metricRegistry;
    private final Collection<FileMetadata> fileMetadataCollection = newHashSet();
    private final Md5SumHelper md5SumHelper;

    public FileMetadataContainer(final MetricRegistry metricRegistry) {
        this(metricRegistry, new Md5SumHelper());
    }

    public FileMetadataContainer(final MetricRegistry metricRegistry, final Md5SumHelper md5SumHelper) {
        this.metricRegistry = metricRegistry;
        this.md5SumHelper = md5SumHelper;
    }

    public void clear() {
        fileMetadataCollection.clear();
    }

    public void addFile(final Path path) {
        try {
            final BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);

            fileMetadataCollection.add(new FileMetadata(path.toString(),
                                                        attributes.creationTime(),
                                                        attributes.lastAccessTime(),
                                                        attributes.lastModifiedTime(),
                                                        Files.size(path)));
        } catch (final Exception e) {
            LOGGER.error("Can't read file [{}] ({})", path, e.getClass().getSimpleName());
        }
    }

    public Set<String> extractDuplicates() {
        return fileMetadataCollection.stream()
                                     // index file metadata elements by file size
                                     .collect(MultimapCollector.toMultimap(metricRegistry, name("multimap", "by-size"), FileMetadata::getSize))

                                     // get a stream of entries
                                     .asMap().entrySet().stream()

                                     // keep duplicates by size only
                                     .filter(e -> e.getValue().size() > 1)

                                     // flatten all duplicates by size
                                     .flatMap(e -> e.getValue().stream())

                                     // index file metadata elements by md5sum
                                     .collect(MultimapCollector.toMultimap(metricRegistry, name("multimap", "by-md5sum"), this::md5sum))

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
                                     .map(AbsolutePathNormalizer.INSTANCE)

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
        Collections.sort(value, FileMetadataComparator.INSTANCE);
        value.remove(0);
    }

}
