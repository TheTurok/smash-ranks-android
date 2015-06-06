package com.garpr.android.data;


import com.garpr.android.misc.Utils;

import org.json.JSONException;
import org.json.JSONObject;


public class JSONSetting extends Setting<JSONObject> {


    JSONSetting(final String key) {
        super(key, null);
    }


    @Override
    public JSONObject get() {
        final String string = readSharedPreferences().getString(mKey, null);

        if (Utils.validStrings(string)) {
            try {
                return new JSONObject(string);
            } catch (final JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }


    @Override
    public void set(final JSONObject newValue) {
        final String newValueString;

        if (newValue == null) {
            newValueString = null;
        } else {
            newValueString = newValue.toString();
        }

        writeSharedPreferences().putString(mKey, newValueString).apply();
    }


}
