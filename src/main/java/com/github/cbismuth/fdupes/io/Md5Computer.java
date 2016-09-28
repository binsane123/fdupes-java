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

import com.codahale.metrics.Timer;
import com.github.cbismuth.fdupes.container.immutable.PathElement;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.zeroturnaround.exec.ProcessExecutor;

import static com.codahale.metrics.MetricRegistry.name;
import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class Md5Computer {

    private static final Logger LOGGER = getLogger(Md5Computer.class);

    public String compute(final PathElement pathElement) {
        Preconditions.checkNotNull(pathElement, "null file metadata");

        try (final Timer.Context ignored = getMetricRegistry().timer(name("md5", "timer")).time()) {
            return doIt(pathElement);
        } catch (final Exception e) {
            LOGGER.error("Can't compute MD5 from file [{}] ([{}]: [{}])",
                         pathElement.getPath(), e.getClass().getSimpleName(), e.getMessage());

            return randomUUID().toString();
        }
    }

    private String doIt(final PathElement element) {
        try {
            return new ProcessExecutor().command(getNativeMd5Command(element))
                                        .readOutput(true)
                                        .execute()
                                        .outputString()
                                        .split("\\s")[1];
        } catch (final Throwable e) {
            throw Throwables.propagate(e);
        }
    }

    private Iterable<String> getNativeMd5Command(final PathElement element) {
        return newArrayList("openssl", "md5", element.getPath().toString());
    }

}
