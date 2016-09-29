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

package com.github.cbismuth.fdupes.io;

import com.github.cbismuth.fdupes.collect.PathAnalyser;
import com.github.cbismuth.fdupes.container.immutable.PathElement;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;

public class PathOrganizerTest {

    private final PathOrganizer systemUnderTest = new PathOrganizer(new PathAnalyser());

    @Test
    public void testOrganize_onTimestampPath() throws IOException {
        // GIVEN
        final Path destination = Files.createTempDirectory(getClass().getSimpleName());

        final Path actual = Paths.get(destination.toString(), "42-MOV2016010212131401-1.MOV");
        Files.createFile(actual);

        final String workingDirectory = destination.toString();
        final PathElement uniqueElement = new PathElement(actual, Files.readAttributes(actual, BasicFileAttributes.class));
        final List<PathElement> uniquesElements = singletonList(uniqueElement);

        // WHEN
        systemUnderTest.organize(workingDirectory, "sub", uniquesElements);

        // THEN
        final Path expected = Paths.get(destination.toString(), "sub", "2016", "01", "02", "20160102121314.MOV");
        assertTrue(Files.exists(expected));
    }

    @Test
    public void testOrganize_onNoTimestampPath() throws IOException {
        // GIVEN
        final Path destination = Files.createTempDirectory(getClass().getSimpleName());

        final Path actual = Paths.get(destination.toString(), "42-MOV01020304-1.MOV");
        Files.createFile(actual);

        final String workingDirectory = destination.toString();
        final PathElement uniqueElement = new PathElement(actual, Files.readAttributes(actual, BasicFileAttributes.class));
        final List<PathElement> uniquesElements = singletonList(uniqueElement);

        // WHEN
        systemUnderTest.organize(workingDirectory, "sub", uniquesElements);

        // THEN
        final Path expected = Paths.get(destination.toString(), "sub", "misc", "42-MOV01020304-1-1.MOV");
        assertTrue(Files.exists(expected));
    }

}
