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

package com.github.cbismuth.fdupes.stream;

import com.github.cbismuth.fdupes.Main;
import com.github.cbismuth.fdupes.immutable.FileMetadata;
import com.github.cbismuth.fdupes.io.DirectoryWalker;
import com.github.cbismuth.fdupes.io.DuplicatesWriter;
import com.github.cbismuth.fdupes.md5.Md5Computer;
import com.github.cbismuth.fdupes.util.PathUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DirectoryWalkerTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return asList(
            new Object[][] {
                { new Md5Computer() },
                { new Md5Computer(null) },
                { newForceByteComparisonMock() },
                { newNativeMd5SumWithExceptionMock() },
                { newJvmMd5SumWithExceptionMock() }
            }
        );
    }

    private static Md5Computer newForceByteComparisonMock() {
        final Md5Computer mock = Mockito.mock(Md5Computer.class);
        Mockito.when(mock.compute(Mockito.any(FileMetadata.class))).thenReturn(randomUUID().toString());
        Mockito.when(mock.toString()).thenReturn("byte-by-byte");
        return mock;
    }

    private static Md5Computer newNativeMd5SumWithExceptionMock() {
        final Md5Computer mock = Mockito.mock(Md5Computer.class);
        Mockito.when(mock.nativeMd5Sum(Mockito.any(FileMetadata.class))).thenThrow(new RuntimeException());
        Mockito.when(mock.toString()).thenReturn("mock-exception-md5-native");
        return mock;
    }

    private static Md5Computer newJvmMd5SumWithExceptionMock() {
        final Md5Computer mock = Mockito.mock(Md5Computer.class);
        Mockito.when(mock.jvmMd5Sum(Mockito.any(FileMetadata.class))).thenThrow(new RuntimeException());
        Mockito.when(mock.toString()).thenReturn("mock-exception-md5-jvm");
        return mock;
    }

    @Before
    public void setUp() {
        getMetricRegistry().getMetrics()
                           .keySet()
                           .forEach(getMetricRegistry()::remove);
    }

    private static final long UNIQUE_FILES_COUNT = 2L;
    private static final long DIRECTORY_DUPLICATION_FACTOR = 10L;
    private static final long FILE_DUPLICATION_FACTOR = 200L;

    private final PathUtils helper = new PathUtils();

    private final Main systemUnderTest;

    public DirectoryWalkerTest(final Md5Computer md5) {
        final DirectoryWalker walker = new DirectoryWalker(md5);
        final DuplicatesWriter writer = new DuplicatesWriter();

        systemUnderTest = new Main(md5, walker, writer);
    }

    @Test
    public void testExtractDuplicates_fakePath() throws Exception {
        // GIVEN
        final Path inputPath = Paths.get(helper.uniqueString());

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launchAndReport(inputPath.toString())
        );

        // THEN
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_emptyPaths() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(helper.uniqueString());
        parentDirectory.toFile().deleteOnExit();

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.add(helper.createEmptyTempDirectory(parentDirectory));
        sources.add(helper.createEmptyTempDirectory(parentDirectory));
        sources.add(helper.createEmptyTempDirectory(parentDirectory));

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launchAndReport(parentDirectory.toString())
        );

        // THEN
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_singleEmptyFile() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(helper.uniqueString());
        parentDirectory.toFile().deleteOnExit();

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.add(helper.createSingleEmptyFile(parentDirectory));

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launchAndReport(parentDirectory.toString())
        );

        // THEN
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_duplicatesBySizeOnly() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(helper.uniqueString());
        parentDirectory.toFile().deleteOnExit();

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.addAll(helper.createNewSetWithDuplicatesBySize(parentDirectory, DIRECTORY_DUPLICATION_FACTOR, UNIQUE_FILES_COUNT));

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launchAndReport(parentDirectory.toString())
        );

        // THEN
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_duplicatesByMd5SumOnly() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(helper.uniqueString());
        parentDirectory.toFile().deleteOnExit();

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.addAll(helper.createNewSetWithDuplicatesByMd5Sum(parentDirectory, UNIQUE_FILES_COUNT, DIRECTORY_DUPLICATION_FACTOR, FILE_DUPLICATION_FACTOR));

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launchAndReport(parentDirectory.toString())
        );

        // THEN
        final long expectedDuplicatesByMd5SumCount = UNIQUE_FILES_COUNT * FILE_DUPLICATION_FACTOR * DIRECTORY_DUPLICATION_FACTOR - UNIQUE_FILES_COUNT;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_all() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(helper.uniqueString());
        parentDirectory.toFile().deleteOnExit();

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.add(helper.createEmptyTempDirectory(parentDirectory));
        sources.add(helper.createSingleEmptyFile(parentDirectory));
        sources.addAll(helper.createNewSetWithDuplicatesBySize(parentDirectory, DIRECTORY_DUPLICATION_FACTOR, UNIQUE_FILES_COUNT));
        sources.addAll(helper.createNewSetWithDuplicatesByMd5Sum(parentDirectory, UNIQUE_FILES_COUNT, DIRECTORY_DUPLICATION_FACTOR, FILE_DUPLICATION_FACTOR));

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launchAndReport(parentDirectory.toString())
        );

        // THEN
        final long expectedDuplicatesByMd5SumCount = UNIQUE_FILES_COUNT * FILE_DUPLICATION_FACTOR * DIRECTORY_DUPLICATION_FACTOR - UNIQUE_FILES_COUNT;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

}
