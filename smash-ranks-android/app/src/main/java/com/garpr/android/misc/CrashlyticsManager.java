package com.garpr.android.misc;


import com.crashlytics.android.Crashlytics;
import com.garpr.android.App;
import com.garpr.android.BuildConfig;

import io.fabric.sdk.android.Fabric;


public final class CrashlyticsManager {


    public static void initialize() {
        Fabric.with(App.getContext(), new Crashlytics());
        Crashlytics.getInstance().core.setBool(Constants.DEBUG, BuildConfig.DEBUG);
    }


    public static void log(final int priority, final String tag, final String msg) {
        throwIfInvalid(tag);
        Crashlytics.log(priority, tag, msg);
    }


    public static void logException(final Throwable throwable) {
        Crashlytics.logException(throwable);
    }


    public static void setBool(final String key, final boolean value) {
        throwIfInvalid(key);
        Crashlytics.setBool(key, value);
    }


    public static void setInt(final String key, final int value) {
        throwIfInvalid(key);
        Crashlytics.setInt(key, value);
    }


    public static void setString(final String key, final String value) {
        throwIfInvalid(key);
        Crashlytics.setString(key, value);
    }


    private static void throwIfInvalid(final String key) {
        if (!Utils.validStrings(key)) {
            throw new IllegalArgumentException("key (" + key + ") can't be invalid");
        }
    }


}
