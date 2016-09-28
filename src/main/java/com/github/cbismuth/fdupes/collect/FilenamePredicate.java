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

import com.google.common.base.Preconditions;
import org.springframework.stereotype.Component;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableCollection;

@Component
public final class FilenamePredicate implements DirectoryStream.Filter<Path> {

    public static final Collection<String> FILENAME_STOP_WORDS = unmodifiableCollection(newArrayList(
        // OS X
        ".ds_store",

        // Windows
        "Icon\r",
        "thumbs.db",
        "desktop.ini",
        "$Recycle.Bin",

        // Synology
        ".SYNOPPSDB",
        ".SynologyWorkingDirectory",
        "@eadir",
        "@sharebin",
        "@SynologyCloudSync",
        "cloudsync_encrypt.info",
        "#recycle"
    ));

    @Override
    public boolean accept(final Path path) {
        Preconditions.checkNotNull(path, "null path");

        final boolean isAllowedDirectory = Files.isDirectory(path)
                                           && Files.isReadable(path)
                                           && !Files.isSymbolicLink(path)
                                           && !isHiddenFile(path.toString())
                                           && !containsForbiddenSubstring(path, FILENAME_STOP_WORDS);

        final boolean isAllowedFile = !Files.isDirectory(path)
                                      && Files.isReadable(path)
                                      && !Files.isSymbolicLink(path)
                                      && !isHiddenFile(path.toString())
                                      && !containsForbiddenSubstring(path, FILENAME_STOP_WORDS);

        return isAllowedDirectory || isAllowedFile;
    }

    private boolean isHiddenFile(final String name) {
        return name.startsWith(".");
    }

    private boolean containsForbiddenSubstring(final Path path, final Collection<String> exclusionList) {
        for (final String s : exclusionList) {
            final Locale locale = Locale.getDefault();

            final String actual = path.toString().toLowerCase(locale);
            final String forbidden = s.toLowerCase(locale);

            if (actual.contains(forbidden)) {
                return true;
            }
        }

        return false;
    }

}
