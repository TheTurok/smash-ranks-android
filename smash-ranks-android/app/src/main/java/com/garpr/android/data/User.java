package com.garpr.android.data;


import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.garpr.android.models.Player;
import com.garpr.android.models.Region;

import org.json.JSONException;
import org.json.JSONObject;


public final class User {


    private static final String CNAME = User.class.getCanonicalName();
    private static final String KEY_PLAYER = "KEY_PLAYER";
    private static final String KEY_REGION = "KEY_REGION";

    private static User sUser;

    private Player mPlayer;
    private Region mRegion;




    public static Player getPlayer() {
        return getUser().mPlayer;
    }


    public static Region getRegion() {
        return getUser().mRegion;
    }


    private static User getUser() {
        if (sUser == null) {
            loadUser();
        }

        return sUser;
    }


    private static void loadUser() {
        sUser = new User();

        final SharedPreferences sPreferences = Settings.get(CNAME);

        try {
            final String playerString = sPreferences.getString(KEY_PLAYER, null);
            final JSONObject playerJSON = new JSONObject(playerString);
            sUser.mPlayer = new Player(playerJSON);

            final String regionString = sPreferences.getString(KEY_REGION, null);
            final JSONObject regionJSON = new JSONObject(regionString);
            sUser.mRegion = new Region(regionJSON);
        } catch (final JSONException e) {
            // this should never happen
            throw new RuntimeException(e);
        }
    }


    private static void saveUser() {
        final User user = getUser();

        final JSONObject playerJSON = user.mPlayer.toJSON();
        final String playerString = playerJSON.toString();

        final JSONObject regionJSON = user.mRegion.toJSON();
        final String regionString = regionJSON.toString();

        final Editor editor = Settings.edit(CNAME);
        editor.putString(KEY_PLAYER, playerString);
        editor.putString(KEY_REGION, regionString);
        editor.apply();
    }


}
