import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

public class FileMetadataContainerTest {

    private static final Logger LOGGER = getLogger(FileMetadataContainerTest.class);

    private final DuplicateFileTreeWalker systemUnderTest = new DuplicateFileTreeWalker();

    @Test
    public void testExtractDuplicates() throws Exception {
        // GIVEN - duplicates
        final String workingDirectory = System.getProperty("user.dir");

        final Collection<Path> originals = listClassFiles(Paths.get(workingDirectory));
        final Path tempDirectory = createTempDirectory(randomUUID().toString());
        final int duplicatesCount = 4;

        duplicateClassFiles(originals, tempDirectory, duplicatesCount);

        // GIVEN - non-duplicate
        Files.createFile(Paths.get(tempDirectory.toString(), randomUUID().toString()));

        // WHEN
        final List<String> rootPaths = ImmutableList.of(tempDirectory.toString(), tempDirectory.toString());
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(rootPaths);

        // THEN
        final long expected = (long) (originals.size() * duplicatesCount);
        final long actual = (long) duplicates.size();

        assertEquals(expected, actual);
    }

    private Collection<Path> listClassFiles(final Path path) {
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

    private void duplicateClassFiles(final Iterable<Path> paths, final Path tempDirectory, final int duplicatesCount) {
        paths.forEach(source -> {
            try {
                final int totalCount = duplicatesCount + 1;

                for (int i = 0; i < totalCount; i++) {
                    duplicateFile(tempDirectory, source, String.valueOf(i));
                }
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
        });
    }

    private void duplicateFile(final Path tempDirectory, final Path source, final String suffix) throws IOException {
        final Path target = Paths.get(tempDirectory.toString(), format("%s-%s", source.getFileName(), suffix));

        final Path copy = Files.copy(source, target, COPY_ATTRIBUTES);
        assertTrue(Files.isRegularFile(copy, NOFOLLOW_LINKS));

        LOGGER.info("File duplicated at [{}]", copy);
    }

}
