package com.garpr.android.misc;


import android.text.TextUtils;
import android.view.MenuItem;

import java.io.Closeable;
import java.io.IOException;
import java.util.Random;


public final class Utils {


    public static final Random RANDOM;




    static {
        RANDOM = new Random();
    }


    public static boolean areAnyMenuItemsNull(final MenuItem... items) {
        if (items == null || items.length == 0) {
            return true;
        }

        for (final MenuItem item : items) {
            if (item == null) {
                return true;
            }
        }

        return false;
    }


    /**
     * Allows you to safely and easily close a bunch of stream-type objects. Note that SQL objects
     * absolutely can't be closed using this method (this is a bug with Android in versions before
     * API 16).
     */
    public static void closeCloseables(final Closeable... closeables) {
        if (closeables != null && closeables.length >= 1) {
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


    public static void hideMenuItems(final MenuItem... items) {
        setMenuItemsVisibility(false, items);
    }


    private static void setMenuItemsVisibility(final boolean visible, final MenuItem... items) {
        if (items != null && items.length >= 1) {
            for (final MenuItem item : items) {
                if (item != null) {
                    item.setVisible(visible);
                }
            }
        }
    }


    public static void showMenuItems(final MenuItem... items) {
        setMenuItemsVisibility(true, items);
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
