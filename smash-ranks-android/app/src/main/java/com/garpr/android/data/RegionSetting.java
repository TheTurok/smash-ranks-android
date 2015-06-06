package com.garpr.android.data;


import com.garpr.android.models.Region;

import org.json.JSONException;
import org.json.JSONObject;


public class RegionSetting extends Setting<Region> {


    private final JSONSetting mJSONSetting;




    RegionSetting(final String key) {
        super(key);
        mJSONSetting = new JSONSetting(key);
    }


    @Override
    public Region get() {
        final JSONObject json = mJSONSetting.get();
        final Region region;

        if (json == null) {
            region = null;
        } else {
            try {
                region = new Region(json);
            } catch (final JSONException e) {
                throw new RuntimeException(e);
            }
        }

        return region;
    }


    @Override
    public void set(final Region newValue) {
        final JSONObject json;

        if (newValue == null) {
            json = null;
        } else {
            json = newValue.toJSON();
        }

        mJSONSetting.set(json);
    }


}
