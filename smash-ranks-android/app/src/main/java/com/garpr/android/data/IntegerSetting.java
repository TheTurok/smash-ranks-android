package com.garpr.android.data;


public class IntegerSetting extends Setting<Integer> {


    IntegerSetting(final String key, final Integer defaultValue) {
        super(key, defaultValue);
    }


    @Override
    public Integer get() {
        return readSharedPreferences().getInt(mKey, mDefaultValue);
    }


    @Override
    public void set(final Integer newValue) {
        writeSharedPreferences().putInt(mKey, newValue).apply();
    }


}
