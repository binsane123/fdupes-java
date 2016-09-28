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

import com.github.cbismuth.fdupes.Launcher;
import com.github.cbismuth.fdupes.Main;
import com.github.cbismuth.fdupes.io.PathHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class, webEnvironment = RANDOM_PORT)
public class DirectoryWalkerTest {

    @Before
    public void setUp() {
        getMetricRegistry().getMetrics()
                           .keySet()
                           .forEach(getMetricRegistry()::remove);
    }

    private static final long UNIQUE_FILES_COUNT = 2L;
    private static final long DIRECTORY_DUPLICATION_FACTOR = 10L;
    private static final long FILE_DUPLICATION_FACTOR = 20L;

    @Autowired
    private PathHelper pathHelper;
    @Autowired
    private Launcher systemUnderTest;

    @Test
    public void testExtractDuplicates_fakePath() throws Exception {
        // GIVEN
        final Path inputPath = Paths.get(pathHelper.uniqueString());

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launch(singleton(inputPath.toString()))
        );

        // THEN
        final long expectedDuplicatesByMd5Count = 0L;
        final long actualDuplicatesByMd5Count = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5Count, actualDuplicatesByMd5Count);
    }

    @Test
    public void testExtractDuplicates_emptyPaths() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(pathHelper.uniqueString());
        parentDirectory.toFile().deleteOnExit();

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.add(pathHelper.createEmptyTempDirectory(parentDirectory));
        sources.add(pathHelper.createEmptyTempDirectory(parentDirectory));
        sources.add(pathHelper.createEmptyTempDirectory(parentDirectory));

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launch(singleton(parentDirectory.toString()))
        );

        // THEN
        final long expectedDuplicatesByMd5Count = 0L;
        final long actualDuplicatesByMd5Count = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5Count, actualDuplicatesByMd5Count);
    }

    @Test
    public void testExtractDuplicates_singleEmptyFile() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(pathHelper.uniqueString());
        parentDirectory.toFile().deleteOnExit();

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.add(pathHelper.createSingleEmptyFile(parentDirectory));

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launch(singleton(parentDirectory.toString()))
        );

        // THEN
        final long expectedDuplicatesByMd5Count = 0L;
        final long actualDuplicatesByMd5Count = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5Count, actualDuplicatesByMd5Count);
    }

    @Test
    public void testExtractDuplicates_duplicatesBySizeOnly() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(pathHelper.uniqueString());
        parentDirectory.toFile().deleteOnExit();

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.addAll(pathHelper.createNewSetWithDuplicatesBySize(parentDirectory, DIRECTORY_DUPLICATION_FACTOR, UNIQUE_FILES_COUNT));

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launch(singleton(parentDirectory.toString()))
        );

        // THEN
        final long expectedDuplicatesByMd5Count = 0L;
        final long actualDuplicatesByMd5Count = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5Count, actualDuplicatesByMd5Count);
    }

    @Test
    public void testExtractDuplicates_duplicatesByMd5Only() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(pathHelper.uniqueString());
        parentDirectory.toFile().deleteOnExit();

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.addAll(pathHelper.createNewSetWithDuplicatesByMd5(parentDirectory, UNIQUE_FILES_COUNT, DIRECTORY_DUPLICATION_FACTOR, FILE_DUPLICATION_FACTOR));

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launch(singleton(parentDirectory.toString()))
        );

        // THEN
        final long expectedDuplicatesByMd5Count = UNIQUE_FILES_COUNT * FILE_DUPLICATION_FACTOR * DIRECTORY_DUPLICATION_FACTOR - UNIQUE_FILES_COUNT;
        final long actualDuplicatesByMd5Count = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5Count, actualDuplicatesByMd5Count);
    }

    @Test
    public void testExtractDuplicates_all() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(pathHelper.uniqueString());
        parentDirectory.toFile().deleteOnExit();

        // GIVEN
        final Collection<Path> sources = newArrayList();
        sources.add(pathHelper.createEmptyTempDirectory(parentDirectory));
        sources.add(pathHelper.createSingleEmptyFile(parentDirectory));
        sources.addAll(pathHelper.createNewSetWithDuplicatesBySize(parentDirectory, DIRECTORY_DUPLICATION_FACTOR, UNIQUE_FILES_COUNT));
        sources.addAll(pathHelper.createNewSetWithDuplicatesByMd5(parentDirectory, UNIQUE_FILES_COUNT, DIRECTORY_DUPLICATION_FACTOR, FILE_DUPLICATION_FACTOR));

        // WHEN
        final Collection<String> duplicates = Files.readAllLines(
            systemUnderTest.launch(singleton(parentDirectory.toString()))
        );

        // THEN
        final long expectedDuplicatesByMd5Count = UNIQUE_FILES_COUNT * FILE_DUPLICATION_FACTOR * DIRECTORY_DUPLICATION_FACTOR - UNIQUE_FILES_COUNT;
        final long actualDuplicatesByMd5Count = (long) duplicates.size();
        assertEquals(expectedDuplicatesByMd5Count, actualDuplicatesByMd5Count);
    }

}
