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

import com.github.cbismuth.fdupes.immutable.FileMetadata;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.unmodifiableCollection;
import static org.slf4j.LoggerFactory.getLogger;

public class FileMetadataContainer {

    private static final Logger LOGGER = getLogger(FileMetadataContainer.class);

    private final Collection<FileMetadata> fileMetadataCollection = newHashSet();

    public void clear() {
        fileMetadataCollection.clear();
    }

    public void addFile(final Path path) {
        Preconditions.checkNotNull(path, "null path");

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

    public Collection<FileMetadata> getElements() {
        return unmodifiableCollection(fileMetadataCollection);
    }

}
