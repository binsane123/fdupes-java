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

import com.github.cbismuth.fdupes.immutable.FileMetadata;
import com.google.common.base.Throwables;
import com.google.common.primitives.UnsignedBytes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class ByteBuffer implements AutoCloseable {

    private static final int BUFFER_SIZE = 64 * 1024;

    private final FileMetadata fileMetadata;
    private final BufferedInputStream inputStream;

    private int offset;
    private int length;
    private final byte[] buffer = new byte[BUFFER_SIZE];

    public ByteBuffer(final FileMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;

        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(this.fileMetadata.getAbsolutePath())));
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public String getByteString() {
        return offset < buffer.length && length > 0 ? UnsignedBytes.join(":", Arrays.copyOf(buffer, length)) : "";
    }

    public void read() {
        try {
            if (offset < buffer.length && (length = inputStream.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += length;
            }
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }

}
