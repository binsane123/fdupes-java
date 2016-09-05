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

package fdupes.io;

import com.codahale.metrics.MetricRegistry;
import fdupes.container.FileMetadataContainer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static com.codahale.metrics.MetricRegistry.name;
import static java.nio.file.Files.isDirectory;
import static org.slf4j.LoggerFactory.getLogger;

public class DuplicateFileTreeWalker {

    private static final Logger LOGGER = getLogger(DuplicateFileTreeWalker.class);

    private final MetricRegistry metricRegistry;
    private final FileMetadataContainer fileMetadataContainer;

    public DuplicateFileTreeWalker(final MetricRegistry metricRegistry) {
        this(metricRegistry, new FileMetadataContainer(metricRegistry));
    }

    public DuplicateFileTreeWalker(final MetricRegistry metricRegistry, final FileMetadataContainer fileMetadataContainer) {
        this.metricRegistry = metricRegistry;
        this.fileMetadataContainer = fileMetadataContainer;
    }

    public Set<String> extractDuplicates(final Iterable<String> inputPaths) {
        fileMetadataContainer.clear();

        inputPaths.forEach(rootPath -> {
            final Path path = Paths.get(rootPath);

            if (isDirectory(path)) {
                handleDirectory(path);
            } else {
                LOGGER.warn("[{}] is not a directory", rootPath);
            }
        });

        return fileMetadataContainer.extractDuplicates();
    }

    private void handleDirectory(final Path path) {
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path, e -> isDirectory(e) || !e.toString().startsWith("."))) {
            stream.forEach(p -> {
                if (isDirectory(p)) {
                    metricRegistry.counter(name("walker", "directories", "counter")).inc();

                    handleDirectory(p);
                } else {
                    handleFile(p);
                }
            });
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void handleFile(final Path path) {
        metricRegistry.counter(name("walker", "files", "counter")).inc();

        fileMetadataContainer.addFile(path);
    }

}
