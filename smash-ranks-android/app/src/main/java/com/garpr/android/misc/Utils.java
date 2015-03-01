package com.garpr.android.misc;


import android.text.TextUtils;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;


public final class Utils {


    public static final Random RANDOM;




    static {
        RANDOM = new Random();
    }


    public static boolean areAnyObjectsNull(final Object... objects) {
        if (objects == null || objects.length == 0) {
            return true;
        }

        for (final Object object : objects) {
            if (object == null) {
                return true;
            }
        }

        return false;
    }


    public static String getJSONString(final JSONObject json, final String first,
            final String second) throws JSONException {
        final String string;

        if (json.has(first)) {
            string = json.getString(first);
        } else {
            string = json.getString(second);
        }

        return string;
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
