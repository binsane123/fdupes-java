package com.github.cbismuth.synology.picture.duplicate;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static com.google.common.base.Charsets.UTF_8;
import static java.lang.String.format;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

class FileMetadataContainerTestHelper {

    private static final Logger LOGGER = getLogger(FileMetadataContainerTestHelper.class);

    String uniqueString() {
        return randomUUID().toString();
    }

    Path uniqueTempPath(final Path parentTempPath) {
        return Paths.get(parentTempPath.toString(), uniqueString());
    }

    Path createEmptyFile(final Path tempDirectory) throws IOException {
        return Files.createFile(uniqueTempPath(tempDirectory));
    }

    Path createUniqueFile(final Path tempDirectory) throws IOException {
        return Files.write(uniqueTempPath(tempDirectory), uniqueString().getBytes(UTF_8), CREATE);
    }

    void createUniqueFiles(final Path tempDirectory, final long count) throws IOException {
        for (long i = 0L; i < count; i++) {
            createUniqueFile(tempDirectory);
        }
    }

    Collection<Path> listClassFiles(final Path path) {
        final Collection<Path> classFiles = Lists.newArrayList();

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

    void duplicateClassFiles(final Iterable<Path> paths, final Path tempDirectory, final long duplicatesCount) {
        paths.forEach(source -> {
            try {
                final long totalCount = duplicatesCount + 1L;

                for (long i = 0L; i < totalCount; i++) {
                    duplicateFile(tempDirectory, source, String.valueOf(i));
                }
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
        });
    }

    void duplicateFile(final Path tempDirectory, final Path source, final String suffix) throws IOException {
        final Path target = Paths.get(tempDirectory.toString(), format("%s %s", source.getFileName(), suffix));

        final Path copy = Files.copy(source, target, COPY_ATTRIBUTES);
        assertTrue(Files.isRegularFile(copy, NOFOLLOW_LINKS));

        LOGGER.debug("File duplicated at [{}]", copy);
    }

}
