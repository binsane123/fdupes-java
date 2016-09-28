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
import com.github.cbismuth.fdupes.io.PathEscapeFunction;
import com.github.cbismuth.fdupes.io.PathHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static com.github.cbismuth.fdupes.metrics.MetricRegistrySingleton.getMetricRegistry;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class, webEnvironment = RANDOM_PORT)
public class SimpleDirectoryWalkerTest {

    @Autowired
    private PathHelper pathHelper;
    @Autowired
    private PathEscapeFunction pathEscapeFunction;
    @Autowired
    private Launcher systemUnderTest;

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

        final Collection<Path> filesWithDuplicates = pathHelper.createNewSetWithDuplicatesByMd5(
            parentDirectory,
            distinctFilesCount,
            directoryDuplicationFactor,
            fileDuplicationFactor
        );

        // WHEN
        final List<String> inputAbsolutePaths = filesWithDuplicates.parallelStream()
                                                                   .map(Path::toString)
                                                                   .collect(toList());
        final Path outputDuplicatesReportPath = systemUnderTest.launch(inputAbsolutePaths);
        final Collection<String> actual = Files.readAllLines(outputDuplicatesReportPath);

        // THEN
        assertEquals(directoryDuplicationFactor * fileDuplicationFactor - distinctFilesCount, actual.size());

        final List<String> escapedAbsolutePathWithDuplicates = filesWithDuplicates.parallelStream()
                                                                                  .map(Path::toString)
                                                                                  .map(s -> pathEscapeFunction.apply(s))
                                                                                  .collect(toList());

        actual.forEach(escapedAbsolutePathWithDuplicates::contains);
    }

}
