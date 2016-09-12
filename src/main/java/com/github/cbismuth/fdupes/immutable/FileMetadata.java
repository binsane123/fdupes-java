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

package com.github.cbismuth.fdupes.immutable;

import com.google.common.base.Preconditions;

import java.nio.file.attribute.FileTime;

public class FileMetadata {

    private final String absolutePath;
    private final FileTime creationTime;
    private final FileTime lastAccessTime;
    private final FileTime lastModifiedTime;
    private final long size;

    public FileMetadata(final String absolutePath,
                        final FileTime creationTime,
                        final FileTime lastAccessTime,
                        final FileTime lastModifiedTime,
                        final long size) {
        Preconditions.checkNotNull(absolutePath, "null absolute path");
        Preconditions.checkNotNull(creationTime, "null creation time");

        this.absolutePath = absolutePath;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.lastModifiedTime = lastModifiedTime;
        this.size = size;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public FileTime getCreationTime() {
        return creationTime;
    }

    public FileTime getLastAccessTime() {
        return lastAccessTime;
    }

    public FileTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public long getSize() {
        return size;
    }

}
