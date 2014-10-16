package com.garpr.android.misc;


import java.io.Closeable;
import java.io.IOException;


public class Utils {


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


}
