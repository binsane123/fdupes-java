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
import com.github.cbismuth.fdupes.io.DirectoryWalker;
import com.github.cbismuth.fdupes.io.DuplicatesWriter;
import com.github.cbismuth.fdupes.io.PathUtils;
import com.github.cbismuth.fdupes.md5.Md5Computer;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class SimpleDirectoryWalkerTest {

    private final PathUtils helper = new PathUtils();

    private final Main systemUnderTest;

    public SimpleDirectoryWalkerTest() {
        final Md5Computer md5 = new Md5Computer();
        final DirectoryWalker walker = new DirectoryWalker(md5);
        final DuplicatesWriter writer = new DuplicatesWriter();

        systemUnderTest = new Main(md5, walker, writer);
    }

    @Before
    public void setUp() {
        getMetricRegistry().getMetrics()
                           .keySet()
                           .forEach(getMetricRegistry()::remove);
    }

    @Test
    public void testExtractDuplicates_simple() throws Exception {
        final Path parentDirectory = Files.createTempDirectory(getClass().getSimpleName());
        parentDirectory.toFile().deleteOnExit();

        // GIVEN
        final int distinctFilesCount = 1;
        final int directoryDuplicationFactor = 8;
        final int fileDuplicationFactor = 100;

        final Collection<Path> filesWithDuplicates = helper.createNewSetWithDuplicatesByMd5Sum(
            parentDirectory,
            distinctFilesCount,
            directoryDuplicationFactor,
            fileDuplicationFactor
        );

        // WHEN
        final Collection<String> actual = Files.readAllLines(
            systemUnderTest.launchAndReport(filesWithDuplicates.parallelStream()
                                                               .map(Path::toString)
                                                               .collect(toList()))
        );

        // THEN
        assertEquals(directoryDuplicationFactor * fileDuplicationFactor - distinctFilesCount, actual.size());

        final List<String> escapedAbsolutePathWithDuplicates = filesWithDuplicates.parallelStream()
                                                                                  .map(Path::toString)
                                                                                  .map(s -> format("\"%s\"", s))
                                                                                  .collect(toList());

        actual.forEach(escapedAbsolutePathWithDuplicates::contains);
    }

}
