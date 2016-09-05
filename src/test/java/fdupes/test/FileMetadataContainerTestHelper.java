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

package fdupes.test;

import com.google.common.base.Throwables;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

public class FileMetadataContainerTestHelper {

    private static final Logger LOGGER = getLogger(FileMetadataContainerTestHelper.class);

    private final long uniqueDirectoriesCount;
    private final long uniqueFilesCount;
    private final long duplicationFactor;

    public FileMetadataContainerTestHelper(final long uniqueDirectoriesCount,
                                           final long uniqueFilesCount,
                                           final long duplicationFactor) {
        this.uniqueDirectoriesCount = uniqueDirectoriesCount;
        this.uniqueFilesCount = uniqueFilesCount;
        this.duplicationFactor = duplicationFactor;
    }

    public Collection<Path> listClassFiles(final Path path) {
        final Collection<Path> classFiles = newArrayList();

        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path, e -> isDirectory(e) || e.toString().endsWith(".class"))) {
            stream.forEach(e -> {
                if (isDirectory(e)) {
                    classFiles.addAll(listClassFiles(e));
                } else {
                    classFiles.add(e);
                }
            });

            return classFiles;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void addEmptyPath(final Collection<Path> rootPaths) throws IOException {
        rootPaths.add(makePathDeeper(createTempDirectory(uniqueString())));
    }

    public void addUniqueEmptyFile(final Collection<Path> rootPaths) throws IOException {
        final Path path = makePathDeeper(createTempDirectory(uniqueString()));

        createEmptyFile(path);

        rootPaths.add(path);
    }

    public void addDuplicatesBySize(final Collection<Path> rootPaths) throws IOException {
        for (long i = 0L; i < uniqueDirectoriesCount; i++) {
            final Path path = makePathDeeper(createTempDirectory(uniqueString()));

            createUniqueFiles(path);

            rootPaths.add(path);
        }
    }

    public void addDuplicatesByMd5Sum(final Iterable<Path> originals, final Collection<Path> rootPaths) throws IOException {
        for (long i = 0L; i < uniqueDirectoriesCount; i++) {
            final Path path = makePathDeeper(createTempDirectory(uniqueString()));

            duplicateClassFiles(originals, path);

            rootPaths.add(path);
        }
    }

    private Path createEmptyFile(final Path rootPath) throws IOException {
        return createFile(rootPath, "".getBytes(UTF_8));

    }

    private void createUniqueFiles(final Path rootPath) throws IOException {
        for (long i = 0L; i < uniqueFilesCount; i++) {
            createUniqueFile(rootPath);
        }
    }

    private Path createUniqueFile(final Path rootPath) throws IOException {
        return createFile(rootPath, uniqueString().getBytes(UTF_8));
    }

    private Path createFile(final Path rootPath, final byte[] content) throws IOException {
        final Path parentPath = makePathDeeper(rootPath);
        final Path filePath = Paths.get(parentPath.toString(), uniqueString());

        createDirectories(parentPath);

        return Files.write(filePath, content);
    }

    private void createDirectories(final Path path) {
        try {
            Files.createDirectories(path);
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private Path makePathDeeper(final Path rootPath) {
        final Path path = Paths.get(rootPath.toString(), uniqueString(), uniqueString());

        createDirectories(path);

        return path;
    }

    private void duplicateClassFiles(final Iterable<Path> paths, final Path tempDirectory) {
        paths.forEach(source -> {
            try {
                for (long i = 0L; i < duplicationFactor; i++) {
                    duplicateFile(tempDirectory, source, String.valueOf(i));
                }
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
        });
    }

    private void duplicateFile(final Path tempDirectory, final Path source, final String suffix) throws IOException {
        final Path target = Paths.get(tempDirectory.toString(), format("%s %s", source.getFileName(), suffix));

        final Path copy = Files.copy(source, target, COPY_ATTRIBUTES);
        assertTrue(Files.isRegularFile(copy, NOFOLLOW_LINKS));

        LOGGER.debug("File duplicated at [{}]", copy);
    }

    private String uniqueString() {
        return randomUUID().toString();
    }

}
