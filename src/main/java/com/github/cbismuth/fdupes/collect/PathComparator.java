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

import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ComparisonChain;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Comparator;

@Component
public class PathComparator implements Comparator<PathElement>, Serializable {

    private static final long serialVersionUID = -1463353231447344113L;

    @Override
    public int compare(final PathElement o1, final PathElement o2) {
        Preconditions.checkNotNull(o1, "null path element 1");
        Preconditions.checkNotNull(o2, "null path element 2");

        try {
            return ComparisonChain.start()
                                  .compare(o1.creationTime(), o2.creationTime())
                                  .compare(o1.lastAccessTime(), o2.lastAccessTime())
                                  .compare(o1.lastModifiedTime(), o2.lastModifiedTime())
                                  .compare(o1.getPath().toString(), o2.getPath().toString())
                                  .result();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
