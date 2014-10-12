package com.garpr.android.misc;


import java.io.Closeable;
import java.io.IOException;


public class Utils {


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
