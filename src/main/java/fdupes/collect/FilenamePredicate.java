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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;

public final class FilenamePredicate implements Predicate<Path> {

    public static final Collection<String> FILENAME_STOP_WORDS = newArrayList(
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
    );

    @Override
    public boolean test(final Path path) {
        return !isHiddenFile(path.toString()) && !containsForbiddenSubstring(path, FILENAME_STOP_WORDS);
    }

    private boolean isHiddenFile(final String name) {
        return name.startsWith(".");
    }

    private boolean containsForbiddenSubstring(final Path path, final Collection<String> exclusionList) {
        for (final String s : exclusionList) {
            // FIXME promote locale value as an application property
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
