package fdups;

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
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

class FileMetadataContainerTestHelper {

    private static final Logger LOGGER = getLogger(FileMetadataContainerTestHelper.class);

    private final long uniqueDirectoriesCount;
    private final long uniqueFilesCount;
    private final long duplicationFactor;

    FileMetadataContainerTestHelper(final long uniqueDirectoriesCount,
                                    final long uniqueFilesCount,
                                    final long duplicationFactor) {
        super();

        this.uniqueDirectoriesCount = uniqueDirectoriesCount;
        this.uniqueFilesCount = uniqueFilesCount;
        this.duplicationFactor = duplicationFactor;
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

    void addEmptyPath(final Collection<Path> rootPaths) throws IOException {
        rootPaths.add(makePathDeeper(createTempDirectory(uniqueString())));
    }

    void addUniqueEmptyFile(final Collection<Path> rootPaths) throws IOException {
        final Path path = makePathDeeper(createTempDirectory(uniqueString()));

        createEmptyFile(path);

        rootPaths.add(path);
    }

    void addDuplicatesBySize(final Collection<Path> rootPaths) throws IOException {
        for (long i = 0L; i < uniqueDirectoriesCount; i++) {
            final Path path = makePathDeeper(createTempDirectory(uniqueString()));

            createUniqueFiles(path);

            rootPaths.add(path);
        }
    }

    void addDuplicatesByMd5Sum(final Iterable<Path> originals, final Collection<Path> rootPaths) throws IOException {
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

        return Files.write(filePath, content, CREATE);
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
