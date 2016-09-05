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

package fdupes.container;

import fdupes.io.FileMetadataDuplicateWriter;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static fdupes.io.FileMetadataDuplicateWriter.NEW_LINE;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class FileMetadataDuplicateWriterTest {

    private final FileMetadataDuplicateWriter systemUnderTest = new FileMetadataDuplicateWriter();

    @Test
    public void testWrite() throws IOException {
        // GIVEN
        final Collection<String> strings = asList("abcd", "xyz");

        // WHEN
        final Path path = systemUnderTest.write(strings);

        // THEN
        final String actual = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertEquals("abcd" + NEW_LINE + "xyz", actual);
    }

}
