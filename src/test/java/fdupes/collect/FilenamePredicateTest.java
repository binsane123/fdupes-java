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

package fdupes.collect;

import com.google.common.base.Throwables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import static fdupes.collect.FilenamePredicate.FILENAME_STOP_WORDS;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

@RunWith(Parameterized.class)
public class FilenamePredicateTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return FILENAME_STOP_WORDS.parallelStream()
                                  .map(s -> new Object[] { s })
                                  .collect(Collectors.toList());
    }

    private static final Logger LOGGER = getLogger(FilenamePredicateTest.class);

    private final String forbiddenSubstring;
    private final FilenamePredicate systemUnderTest = FilenamePredicate.INSTANCE;

    public FilenamePredicateTest(final String forbiddenSubstring) {
        this.forbiddenSubstring = forbiddenSubstring;
    }

    @Test
    public void testAccept() throws IOException {
        final Path path = Files.createTempDirectory(randomUUID().toString());

        createFileWithSubstringInName(path, " a substring ");
        createFileWithSubstringInName(path, forbiddenSubstring);

        final int expectedFilesCount = 1;
        final int actualFilesCount = Files.list(path)
                                          .filter(systemUnderTest::accept)
                                          .collect(Collectors.toList())
                                          .size();

        assertEquals(expectedFilesCount, actualFilesCount);
    }

    private Path createFileWithSubstringInName(final Path parentPath, final String substring) {
        try {
            final String prefix = randomUUID().toString();
            final String suffix = randomUUID().toString();
            final String filename = prefix + substring + suffix;
            final Path filePath = Paths.get(parentPath.toString(), filename);

            final Path file = Files.createFile(filePath);

            LOGGER.debug("Created file [{}]", file);

            return file;
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

}
