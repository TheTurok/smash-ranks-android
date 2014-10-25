package com.garpr.android.misc;


import android.text.TextUtils;

import java.io.Closeable;
import java.io.IOException;


public final class Utils {


    /**
     * Allows you to safely and easily close a bunch of stream-type objects. Note that SQL objects
     * absolutely can't be closed using this method (this is a bug with Android in versions before
     * API 16).
     */
    public static void closeCloseables(final Closeable... closeables) {
        if (closeables != null && closeables.length != 0) {
            for (final Closeable closeable : closeables) {
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (final IOException e) {
                        // this Exception can be safely ignored
                    }
                }
            }
        }
    }


    /**
     * Checks a collection of {@link CharSequence} objects to see if any single one of them is
     * null, empty, or just whitespace.
     *
     * @param sequences
     * The collection of {@link CharSequence} objects to check.
     *
     * @return
     * Returns false if any single one of the given {@link CharSequence} objects is null, empty,
     * or just whitespace.
     */
    public static boolean validStrings(final CharSequence... sequences) {
        if (sequences == null || sequences.length == 0) {
            return false;
        }

        for (final CharSequence sequence : sequences) {
            if (TextUtils.isEmpty(sequence) || TextUtils.getTrimmedLength(sequence) == 0) {
                return false;
            }
        }

        return true;
    }


}
