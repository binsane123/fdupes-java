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
import fdupes.immutable.FileMetadata;
import fdupes.io.ToByteStringFunction;
import fdupes.md5.Md5Computer;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public class DuplicatesFinder {

    private final Md5Computer md5;
    private final StreamHandler handler = new StreamHandler();

    public DuplicatesFinder(final Md5Computer md5) {
        Preconditions.checkNotNull(md5, "null MD5 computer");

        this.md5 = md5;
    }

    public Set<String> extractDuplicates(final Collection<FileMetadata> elements) {
        Preconditions.checkNotNull(elements, "null file metadata collection");

        Stream<FileMetadata> stream = elements.stream();

        stream = handler.removeUniqueFilesByKey(stream, "size", FileMetadata::getSize);
        stream = handler.removeUniqueFilesByKey(stream, "md5", md5::compute);
        stream = handler.removeUniqueFilesByKeyAndOriginals(stream, "bytes", ToByteStringFunction.INSTANCE);

        return handler.extractAbsolutePaths(stream);
    }

}
