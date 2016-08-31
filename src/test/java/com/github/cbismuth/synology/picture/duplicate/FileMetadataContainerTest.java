package com.github.cbismuth.synology.picture.duplicate;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static java.nio.file.Files.createTempDirectory;
import static org.junit.Assert.assertEquals;

public class FileMetadataContainerTest {

    private static final String WORKING_DIRECTORY = System.getProperty("user.dir");

    private static final long UNIQUE_FILES_COUNT = 8L;
    private static final long DUPLICATES_BY_MD5SUM_FACTOR = 4L;

    private final DuplicateFileTreeWalker systemUnderTest = new DuplicateFileTreeWalker();
    private final FileMetadataContainerTestHelper helper = new FileMetadataContainerTestHelper();

    @Test
    public void testExtractDuplicates() throws Exception {
        final Path tempDirectory = createTempDirectory(helper.uniqueString());

        // GIVEN - non-duplicate - an empty file
        helper.createEmptyFile(tempDirectory);

        // GIVEN - duplicates - by size
        helper.createUniqueFiles(tempDirectory, UNIQUE_FILES_COUNT + 1L);

        // GIVEN - duplicates - by md5sum
        final Collection<Path> originals = helper.listClassFiles(Paths.get(WORKING_DIRECTORY));

        helper.duplicateClassFiles(originals, tempDirectory, DUPLICATES_BY_MD5SUM_FACTOR);

        // WHEN - with duplicate input root directories
        final List<String> rootPaths = ImmutableList.of(tempDirectory.toString(), tempDirectory.toString());
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(rootPaths);

        // THEN - registry
        final long expectedDuplicatesByMd5SumCount = (long) originals.size() * DUPLICATES_BY_MD5SUM_FACTOR;
        final long expectedDuplicatesBySize = UNIQUE_FILES_COUNT + expectedDuplicatesByMd5SumCount;

        assertEquals(expectedDuplicatesBySize, getCounterValue("duplicatesBySize.duplicates"));
        assertEquals(expectedDuplicatesByMd5SumCount, getCounterValue("duplicatesByMd5Sum.duplicates"));

        // THEN - output value
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    private long getCounterValue(final String name) {
        return systemUnderTest.getMetricRegistry().counter(name).getCount();
    }

}
