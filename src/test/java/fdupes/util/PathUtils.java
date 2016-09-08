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

package fdupes.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.util.UUID.randomUUID;

public class PathUtils {

    private static final String NEW_LINE = System.getProperty("line.separator");

    public Path createSingleEmptyFile(final Path parentDirectory) throws IOException {
        final Path path = createEmptyTempDirectory(parentDirectory);

        return createEmptyFile(path, uniqueString(), uniqueString());
    }

    public Path createEmptyTempDirectory(final Path parentDirectory) throws IOException {
        return createSubDirectory(parentDirectory, uniqueString());
    }

    public Collection<Path> createNewSetWithDuplicatesBySize(final Path parentDirectory, final long directoryDuplicationFactor, final long fileDuplicationFactor) throws IOException {
        final Collection<Path> destination = newArrayList();

        for (long i = 0L; i < directoryDuplicationFactor; i++) {
            final String subDirectoryName = uniqueString();
            final Path parentSubDirectory = createSubDirectory(parentDirectory, subDirectoryName);

            createUniqueFilesWithSingleUUIDContent(parentSubDirectory, fileDuplicationFactor);

            destination.add(parentSubDirectory);
        }

        return destination;
    }

    public Collection<Path> createNewSetWithDuplicatesByMd5Sum(final Path parentDirectory, final long distinctFilesCount, final long directoryDuplicationFactor, final long fileDuplicationFactor) throws IOException {
        final Collection<Path> originals = createUniqueFilesWithRandomContent(Files.createTempDirectory(uniqueString()), distinctFilesCount);

        final Collection<Path> destination = newArrayList();

        for (long i = 0L; i < directoryDuplicationFactor; i++) {
            final String subDirectoryName = uniqueString();
            final Path parentSubDirectory = createSubDirectory(parentDirectory, subDirectoryName);

            duplicateFiles(originals, parentSubDirectory, fileDuplicationFactor);

            destination.add(parentSubDirectory);
        }

        return destination;
    }

    private Path createEmptyFile(final Path parentDirectory, final String subDirectoryName, final String filename) throws IOException {
        checkIsDirectory(parentDirectory);

        return createFileWithContent(parentDirectory, subDirectoryName, filename, "".getBytes(UTF_8));
    }

    private Collection<Path> createUniqueFilesWithSingleUUIDContent(final Path parentDirectory, final long fileDuplicationFactor) throws IOException {
        checkIsDirectory(parentDirectory);

        final Collection<Path> destination = newArrayList();

        for (long i = 0L; i < fileDuplicationFactor; i++) {
            final Path uniqueFile = createUniqueFileWithSingleUUIDContent(parentDirectory);

            destination.add(uniqueFile);
        }

        return destination;
    }

    private Path createUniqueFileWithSingleUUIDContent(final Path parentDirectory) throws IOException {
        checkIsDirectory(parentDirectory);

        return createFileWithContent(parentDirectory, uniqueString(), uniqueString(), uniqueString().getBytes(UTF_8));
    }

    private Collection<Path> createUniqueFilesWithRandomContent(final Path parentDirectory, final long fileDuplicationFactor) throws IOException {
        checkIsDirectory(parentDirectory);

        final Collection<Path> destination = newArrayList();

        for (long i = 0L; i < fileDuplicationFactor; i++) {
            final Path uniqueFile = createUniqueFileWithRandomContent(parentDirectory);

            destination.add(uniqueFile);
        }

        return destination;
    }

    private static final AtomicLong LINES_COUNT = new AtomicLong(1000);

    private Path createUniqueFileWithRandomContent(final Path parentDirectory) throws IOException {
        checkIsDirectory(parentDirectory);

        final long max = LINES_COUNT.getAndAdd(1);

        final StringBuilder sb = new StringBuilder();
        for (long i = 0; i < max; i++) {
            sb.append(Strings.padStart(String.valueOf(i), 5, '0')).append(NEW_LINE);
        }

        return createFileWithContent(parentDirectory, uniqueString(), uniqueString(), sb.toString().getBytes(UTF_8));
    }

    private Path createFileWithContent(final Path parentDirectory, final String subDirectoryName, final String filename, final byte[] content) throws IOException {
        checkIsDirectory(parentDirectory);

        final Path parentPath = createSubDirectory(parentDirectory, subDirectoryName);
        final Path filePath = Paths.get(parentPath.toString(), filename);

        Files.createDirectories(parentPath);

        return Files.write(filePath, content);
    }

    private Path createSubDirectory(final Path parentDirectory, final String subDirectoryName) throws IOException {
        checkIsDirectory(parentDirectory);

        final Path path = Paths.get(parentDirectory.toString(), subDirectoryName);

        Files.createDirectories(path);

        return path;
    }

    private Collection<Path> duplicateFiles(final Iterable<Path> inputFiles, final Path outputDirectory, final long fileDuplicationFactor) {
        final Collection<Path> duplicatedFiles = newArrayList();

        inputFiles.forEach(inputFile -> {
            try {
                for (long i = 0L; i < fileDuplicationFactor; i++) {
                    final Path duplicatedFile = duplicateFile(inputFile, outputDirectory, String.valueOf(i));

                    duplicatedFiles.add(duplicatedFile);
                }
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
        });

        return duplicatedFiles;
    }

    private Path duplicateFile(final Path inputFile, final Path outputDirectory, final String suffix) throws IOException {
        checkIsRegularFile(inputFile);
        checkIsDirectory(outputDirectory);

        final String outputFilename = format("%s%s", inputFile.getFileName(), suffix);
        final Path duplicatedFile = Paths.get(outputDirectory.toString(), outputFilename);

        return Files.copy(inputFile, duplicatedFile, COPY_ATTRIBUTES);
    }

    private void checkIsRegularFile(final Path path) {
        Preconditions.checkArgument(Files.isRegularFile(path), format("[%s] isn't a regular file", path));
    }

    private void checkIsDirectory(final Path path) {
        Preconditions.checkArgument(Files.isDirectory(path), format("[%s] isn't a directory", path));
    }

    public String uniqueString() {
        return randomUUID().toString();
    }

}
