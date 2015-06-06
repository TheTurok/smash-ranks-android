package com.garpr.android.data;


public class LongSetting extends Setting<Long> {


    LongSetting(final String key, final Long defaultValue) {
        super(key, defaultValue);
    }


    @Override
    public Long get() {
        return readSharedPreferences().getLong(mKey, mDefaultValue);
    }


    @Override
    public void set(final Long newValue) {
        writeSharedPreferences().putLong(mKey, newValue).apply();
    }


}
