package com.garpr.android.settings;


import com.garpr.android.models.Player;

import org.json.JSONException;
import org.json.JSONObject;


public final class PlayerSetting extends Setting<Player> {


    private final JSONSetting mJSONSetting;




    PlayerSetting(final String key) {
        super(key);
        mJSONSetting = new JSONSetting(key);
    }


    @Override
    public Player get() {
        final JSONObject json = mJSONSetting.get();
        final Player player;

        if (json == null) {
            player = null;
        } else {
            try {
                player = new Player(json);
            } catch (final JSONException e) {
                throw new RuntimeException(e);
            }
        }

        return player;
    }


    @Override
    public void set(final Player newValue) {
        mJSONSetting.set(newValue.toJSON());
    }


}
