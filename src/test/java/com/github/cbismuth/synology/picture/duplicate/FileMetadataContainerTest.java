package com.github.cbismuth.synology.picture.duplicate;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Thread.sleep;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class FileMetadataContainerTest {

    private static final String WORKING_DIRECTORY = System.getProperty("user.dir");

    private static final long UNIQUE_DIRECTORIES_COUNT = 16L;
    private static final long UNIQUE_FILES_COUNT = 3L;
    private static final long DUPLICATION_FACTOR = 7L;

    private final DuplicateFileTreeWalker systemUnderTest = new DuplicateFileTreeWalker();
    private final FileMetadataContainerTestHelper helper = new FileMetadataContainerTestHelper(UNIQUE_DIRECTORIES_COUNT,
                                                                                               UNIQUE_FILES_COUNT,
                                                                                               DUPLICATION_FACTOR);

    @Test
    public void testExtractDuplicates_fakePath() throws Exception {
        // GIVEN
        final Collection<Path> originals = helper.listClassFiles(Paths.get(WORKING_DIRECTORY));
        final Collection<Path> rootPaths = newArrayList();

        rootPaths.add(Paths.get(randomUUID().toString()));

        final List<String> rootAbsolutePaths = rootPaths.stream()
                                                        .map(Path::toString)
                                                        .collect(toList());

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(rootAbsolutePaths);

        refreshMetricRegistry();

        // THEN - md5sum from registry
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCountFromRegistry = registryDuplicatesByMd5Sum();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCountFromRegistry);

        // THEN - size from registry
        final long expectedDuplicatesBySize = 0L;
        final long actualDuplicatesBySizeFromRegistry = registryDuplicatesBySize();
        assertEquals(expectedDuplicatesBySize, actualDuplicatesBySizeFromRegistry);

        // THEN - md5sum from output
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_emptyPath() throws Exception {
        // GIVEN
        final Collection<Path> originals = helper.listClassFiles(Paths.get(WORKING_DIRECTORY));
        final Collection<Path> rootPaths = newArrayList();

        helper.addEmptyPath(rootPaths);

        final List<String> rootAbsolutePaths = rootPaths.stream()
                                                        .map(Path::toString)
                                                        .collect(toList());

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(rootAbsolutePaths);

        refreshMetricRegistry();

        // THEN - md5sum from registry
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCountFromRegistry = registryDuplicatesByMd5Sum();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCountFromRegistry);

        // THEN - size from registry
        final long expectedDuplicatesBySize = 0L;
        final long actualDuplicatesBySizeFromRegistry = registryDuplicatesBySize();
        assertEquals(expectedDuplicatesBySize, actualDuplicatesBySizeFromRegistry);

        // THEN - md5sum from output
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_uniqueEmptyFile() throws Exception {
        // GIVEN
        final Collection<Path> originals = helper.listClassFiles(Paths.get(WORKING_DIRECTORY));
        final Collection<Path> rootPaths = newArrayList();

        helper.addUniqueEmptyFile(rootPaths);

        final List<String> rootAbsolutePaths = rootPaths.stream()
                                                        .map(Path::toString)
                                                        .collect(toList());

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(rootAbsolutePaths);

        refreshMetricRegistry();

        // THEN - md5sum from registry
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCountFromRegistry = registryDuplicatesByMd5Sum();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCountFromRegistry);

        // THEN - size from registry
        final long expectedDuplicatesBySize = 0L;
        final long actualDuplicatesBySizeFromRegistry = registryDuplicatesBySize();
        assertEquals(expectedDuplicatesBySize, actualDuplicatesBySizeFromRegistry);

        // THEN - md5sum from output
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_duplicatesBySizeOnly() throws Exception {
        // GIVEN
        final Collection<Path> originals = helper.listClassFiles(Paths.get(WORKING_DIRECTORY));
        final Collection<Path> rootPaths = newArrayList();

        helper.addDuplicatesBySize(rootPaths);

        final List<String> rootAbsolutePaths = rootPaths.stream()
                                                        .map(Path::toString)
                                                        .collect(toList());

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(rootAbsolutePaths);

        refreshMetricRegistry();

        // THEN - md5sum from registry
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCountFromRegistry = registryDuplicatesByMd5Sum();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCountFromRegistry);

        // THEN - size from registry
        final long expectedDuplicatesBySize = (UNIQUE_DIRECTORIES_COUNT * UNIQUE_FILES_COUNT) - 1L;
        final long actualDuplicatesBySizeFromRegistry = registryDuplicatesBySize();
        assertEquals(expectedDuplicatesBySize, actualDuplicatesBySizeFromRegistry);

        // THEN - md5sum from output
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_duplicatesByMd5SumOnly() throws Exception {
        // GIVEN
        final Collection<Path> originals = helper.listClassFiles(Paths.get(WORKING_DIRECTORY));
        final Collection<Path> rootPaths = newArrayList();

        helper.addDuplicatesByMd5Sum(originals, rootPaths);

        final List<String> rootAbsolutePaths = rootPaths.stream()
                                                        .map(Path::toString)
                                                        .collect(toList());

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(rootAbsolutePaths);

        refreshMetricRegistry();

        // THEN - md5sum from registry
        final long expectedDuplicatesByMd5SumCount = ((long) originals.size() * DUPLICATION_FACTOR * UNIQUE_DIRECTORIES_COUNT) - (long) originals.size();
        final long actualDuplicatesByMd5SumCountFromRegistry = registryDuplicatesByMd5Sum();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCountFromRegistry);

        // THEN - size from registry
        final long expectedDuplicatesBySize = expectedDuplicatesByMd5SumCount;
        final long actualDuplicatesBySizeFromRegistry = registryDuplicatesBySize();
        assertEquals(expectedDuplicatesBySize, actualDuplicatesBySizeFromRegistry);

        // THEN - md5sum from output
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_all() throws Exception {
        // GIVEN
        final Collection<Path> originals = helper.listClassFiles(Paths.get(WORKING_DIRECTORY));
        final Collection<Path> rootPaths = newArrayList();

        helper.addEmptyPath(rootPaths);
        helper.addUniqueEmptyFile(rootPaths);
        helper.addDuplicatesBySize(rootPaths);
        helper.addDuplicatesByMd5Sum(originals, rootPaths);

        final List<String> rootAbsolutePaths = rootPaths.stream()
                                                        .map(Path::toString)
                                                        .collect(toList());

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(rootAbsolutePaths);

        refreshMetricRegistry();

        // THEN - md5sum from registry
        final long expectedDuplicatesByMd5SumCount = ((long) originals.size() * DUPLICATION_FACTOR * UNIQUE_DIRECTORIES_COUNT) - (long) originals.size();
        final long actualDuplicatesByMd5SumCountFromRegistry = registryDuplicatesByMd5Sum();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCountFromRegistry);

        // THEN - size from registry
        final long expectedDuplicatesBySize = expectedDuplicatesByMd5SumCount + (UNIQUE_DIRECTORIES_COUNT * UNIQUE_FILES_COUNT) - 1L;
        final long actualDuplicatesBySizeFromRegistry = registryDuplicatesBySize();
        assertEquals(expectedDuplicatesBySize, actualDuplicatesBySizeFromRegistry);

        // THEN - md5sum from output
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    private void refreshMetricRegistry() throws InterruptedException {
        sleep(SECONDS.toMillis(1L));
    }

    private long registryDuplicatesBySize() {
        return getCounterValue("duplicatesBySize.duplicates");
    }

    private long registryDuplicatesByMd5Sum() {
        return getCounterValue("duplicatesByMd5Sum.duplicates");
    }

    private long getCounterValue(final String name) {
        return systemUnderTest.getMetricRegistry().counter(name).getCount();
    }

}