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

package fdupes.md5;

import com.codahale.metrics.Timer;
import com.google.common.base.Throwables;
import com.google.common.primitives.UnsignedBytes;
import fdupes.container.FileMetadata;
import org.slf4j.Logger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;
import static com.google.common.collect.Lists.newArrayList;
import static fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.slf4j.LoggerFactory.getLogger;

public class Md5SumHelper {

    private static final Logger LOGGER = getLogger(Md5SumHelper.class);

    private final Optional<String> binaryName;

    public Md5SumHelper() {
        this(new Md5SumCommandChecker().getBinaryName());
    }

    public Md5SumHelper(final String binaryName) {
        this.binaryName = Optional.ofNullable(binaryName);
    }

    public String md5sum(final FileMetadata fileMetadata) {
        try (final Timer.Context ignored = getMetricRegistry().timer(name("md5sum", "timer")).time()) {
            return doIt(fileMetadata);
        } catch (final Exception e) {
            LOGGER.error("Can't compute md5sum from file [{}] ({})", fileMetadata.getAbsolutePath(), e.getClass().getSimpleName());
            return randomUUID().toString();
        }
    }

    private String doIt(final FileMetadata fileMetadata) {
        if (binaryName.isPresent()) {
            return nativeMd5Sum(fileMetadata);
        } else {
            return jvmMd5Sum(fileMetadata);
        }
    }

    private String jvmMd5Sum(final FileMetadata fileMetadata) {
        try {
            final String separator = ":";
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final Path path = Paths.get(fileMetadata.getAbsolutePath());
            final byte[] bytes = Files.readAllBytes(path);
            final byte[] digest = md.digest(bytes);

            return UnsignedBytes.join(separator, digest);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private String nativeMd5Sum(final FileMetadata fileMetadata) {
        try {
            return new ProcessExecutor().command(getNativeMd5SumCommand(fileMetadata))
                                        .readOutput(true)
                                        .redirectOutput(Slf4jStream.of("md5sum").asTrace())
                                        .execute()
                                        .outputUTF8()
                                        .split("\\s")[0];
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private Iterable<String> getNativeMd5SumCommand(final FileMetadata fileMetadata) {
        final Collection<String> command = newArrayList();

        if (binaryName.isPresent() && Objects.equals("md5sum", binaryName.get())) {
            command.add("md5sum");
        } else if (binaryName.isPresent() && Objects.equals("md5", binaryName.get())) {
            command.add("md5");
            command.add("-q");
        } else {
            throw new UnsupportedOperationException(format("Unsupported binary name [%s]!", binaryName));
        }

        command.add(fileMetadata.getAbsolutePath());

        return command;
    }

    @Override
    public String toString() {
        return binaryName.isPresent()? binaryName.get() : "";
    }

}
