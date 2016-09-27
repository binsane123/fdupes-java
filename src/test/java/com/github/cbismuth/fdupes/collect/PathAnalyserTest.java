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

package com.github.cbismuth.fdupes.collect;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class PathAnalyserTest {

    private final PathAnalyser systemUnderTest = new PathAnalyser();

    @Test
    public void testGetTimestampPath_onMatch_notExisting() throws IOException {
        final Path destination = Files.createTempDirectory(getClass().getSimpleName());
        final Path path = Paths.get("/somewhere/on/disk/20160102121314-1.JPG");

        final Optional<Path> actual = systemUnderTest.getTimestampPath(destination, path);
        final Path expected = Paths.get(destination.toString(),
                                        "2016", "01", "02",
                                        "20160102121314.JPG");

        assertEquals(expected.toString(), actual.get().toString());
    }

    @Test
    public void testGetTimestampPath_onMatch_existing() throws IOException {
        final Path destination = Files.createTempDirectory(getClass().getSimpleName());
        final Path path = Paths.get(destination.toString(),
                                    "2016", "01", "02",
                                    "20160102121314.JPG");

        Files.createDirectories(Paths.get(destination.toString(), "2016", "01", "02"));
        final Path created1 = Files.createFile(Paths.get(destination.toString(),
                                                         "2016", "01", "02",
                                                         "20160102121314.JPG"));
        final Path created2 = Files.createFile(Paths.get(destination.toString(),
                                                         "2016", "01", "02",
                                                         "20160102121314-1.JPG"));

        final Optional<Path> actual = systemUnderTest.getTimestampPath(destination, path);
        final Path expected = Paths.get(destination.toString(),
                                        "2016", "01", "02",
                                        "20160102121314-2.JPG");

        assertEquals(expected.toString(), actual.get().toString());

        Files.delete(created1);
        Files.delete(created2);
    }

    @Test
    public void testGetTimestampPath_onNotMatch() throws IOException {
        final Path destination = Files.createTempDirectory(getClass().getSimpleName());
        final Path path = Paths.get("/somewhere/on/disk/2016-01-02-12-13-14.JPG");

        final Optional<Path> actual = systemUnderTest.getTimestampPath(destination, path);

        assertEquals(Optional.empty(), actual);
    }

}
