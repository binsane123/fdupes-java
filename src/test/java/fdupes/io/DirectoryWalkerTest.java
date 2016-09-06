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

import fdupes.md5.Md5SumHelper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DirectoryWalkerTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return asList(
            new Object[][] {
                // test with native support if present
                { new Md5SumHelper() },
                // test with jvm md5 implementation
                { new Md5SumHelper(null) }
            }
        );
    }

    @After
    public void tearDown() {
        getMetricRegistry().getMetrics()
                           .keySet()
                           .forEach(getMetricRegistry()::remove);
    }

    private static final long UNIQUE_FILES_COUNT = 3L;
    private static final long DIRECTORY_DUPLICATION_FACTOR = 10L;
    private static final long FILE_DUPLICATION_FACTOR = 20L;

    private final PathHelper helper = new PathHelper();

    private final DirectoryWalker systemUnderTest;

    public DirectoryWalkerTest(final Md5SumHelper md5SumHelper) {
        systemUnderTest = new DirectoryWalker(md5SumHelper);
    }

    @Test
    public void testExtractDuplicates_fakePath() throws Exception {
        // GIVEN
        final Path inputPath = Paths.get(helper.uniqueString());

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(inputPath.toString());

        // THEN
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_emptyPaths() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(helper.uniqueString());

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.add(helper.createEmptyTempDirectory(parentDirectory));
        sources.add(helper.createEmptyTempDirectory(parentDirectory));
        sources.add(helper.createEmptyTempDirectory(parentDirectory));

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(parentDirectory.toString());

        // THEN
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_singleEmptyFile() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(helper.uniqueString());

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.add(helper.createSingleEmptyFile(parentDirectory));

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(parentDirectory.toString());

        // THEN
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_duplicatesBySizeOnly() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(helper.uniqueString());

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.addAll(helper.createNewSetWithDuplicatesBySize(parentDirectory, DIRECTORY_DUPLICATION_FACTOR, UNIQUE_FILES_COUNT));

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(parentDirectory.toString());

        // THEN
        final long expectedDuplicatesByMd5SumCount = 0L;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_duplicatesByMd5SumOnly() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(helper.uniqueString());

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.addAll(helper.createNewSetWithDuplicatesByMd5Sum(parentDirectory, UNIQUE_FILES_COUNT, DIRECTORY_DUPLICATION_FACTOR, FILE_DUPLICATION_FACTOR));

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(parentDirectory.toString());

        // THEN
        final long expectedDuplicatesByMd5SumCount = UNIQUE_FILES_COUNT * FILE_DUPLICATION_FACTOR * DIRECTORY_DUPLICATION_FACTOR - UNIQUE_FILES_COUNT;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

    @Test
    public void testExtractDuplicates_all() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(helper.uniqueString());

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.add(helper.createEmptyTempDirectory(parentDirectory));
        sources.add(helper.createSingleEmptyFile(parentDirectory));
        sources.addAll(helper.createNewSetWithDuplicatesBySize(parentDirectory, DIRECTORY_DUPLICATION_FACTOR, UNIQUE_FILES_COUNT));
        sources.addAll(helper.createNewSetWithDuplicatesByMd5Sum(parentDirectory, UNIQUE_FILES_COUNT, DIRECTORY_DUPLICATION_FACTOR, FILE_DUPLICATION_FACTOR));

        // WHEN
        final Collection<String> duplicates = systemUnderTest.extractDuplicates(parentDirectory.toString());

        // THEN
        final long expectedDuplicatesByMd5SumCount = UNIQUE_FILES_COUNT * FILE_DUPLICATION_FACTOR * DIRECTORY_DUPLICATION_FACTOR - UNIQUE_FILES_COUNT;
        final long actualDuplicatesByMd5SumCount = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5SumCount, actualDuplicatesByMd5SumCount);
    }

}
