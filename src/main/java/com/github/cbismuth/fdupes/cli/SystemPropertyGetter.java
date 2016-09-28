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

package com.github.cbismuth.fdupes.cli;

import org.apache.spark.network.util.JavaUtils;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SystemPropertyGetter {

    private static final Logger LOGGER = getLogger(SystemPropertyGetter.class);

    public static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

    private final Environment environment;

    public SystemPropertyGetter(final Environment environment) {
        this.environment = environment;

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(getParallelism()));
    }

    public int getBufferSize() {
        final String property = environment.getProperty("fdupes.buffer.size", String.class, "4k");

        try {
            return Math.toIntExact(JavaUtils.byteStringAsBytes(property));
        } catch (final NumberFormatException | ArithmeticException ignored) {
            LOGGER.error("Unrecognized buffer size format [{}] fallback to [{}] bytes", property, DEFAULT_BUFFER_SIZE);

            return DEFAULT_BUFFER_SIZE;
        }
    }

    public int getParallelism() {
        return environment.getProperty("fdupes.parallelism", Integer.class, 1);
    }

    public boolean doOrganize() {
        return environment.getProperty("fdupes.organize", Boolean.class, false);
    }

}
