package com.garpr.android.settings;


import android.content.SharedPreferences;

import com.garpr.android.misc.Utils;


public abstract class Setting<T> {


    final T mDefaultValue;
    final String mKey;




    Setting(final String key) {
        this(key, null);
    }


    Setting(final String key, final T defaultValue) {
        if (!Utils.validStrings(key)) {
            throw new IllegalArgumentException("key can't be null / empty / whitespace");
        }

        mKey = key;
        mDefaultValue = defaultValue;
    }


    public void delete() {
        writeSharedPreferences().remove(mKey).apply();
    }


    public boolean exists() {
        return readSharedPreferences().contains(mKey);
    }


    public abstract T get();


    final SharedPreferences readSharedPreferences() {
        return Settings.get();
    }


    public abstract void set(final T newValue);


    final SharedPreferences.Editor writeSharedPreferences() {
        return Settings.edit();
    }


}
