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

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ComparisonChain;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;

public class PathComparator implements Comparator<Path> {

    public static final PathComparator INSTANCE = new PathComparator();

    @Override
    public int compare(final Path o1, final Path o2) {
        Preconditions.checkNotNull(o1, "null path 1");
        Preconditions.checkNotNull(o2, "null path 2");

        try {
            final BasicFileAttributes a1 = Files.readAttributes(o1, BasicFileAttributes.class);
            final BasicFileAttributes a2 = Files.readAttributes(o2, BasicFileAttributes.class);

            return ComparisonChain.start()
                                  .compare(a1.creationTime(), a2.creationTime())
                                  .compare(a1.lastAccessTime(), a2.lastAccessTime())
                                  .compare(a1.lastModifiedTime(), a2.lastModifiedTime())
                                  .compare(o1.toString(), o2.toString())
                                  .result();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
