package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;

import org.json.JSONException;
import org.json.JSONObject;


public final class User {


    private static final String CNAME = "com.garpr.android.data.User";
    private static final String KEY_PLAYER = "KEY_PLAYER";
    private static final String KEY_REGION = "KEY_REGION";
    private static final String TAG = "User";

    private static User sUser;

    private Player mPlayer;
    private Region mRegion;




    public static boolean areWeInTheUsersRegion() {
        final Region usersRegion = getRegion();
        final Region settingsRegion = Settings.getRegion();
        return usersRegion.equals(settingsRegion);
    }


    public static Player getPlayer() {
        return getUser().mPlayer;
    }


    public static Region getRegion() {
        return getUser().mRegion;
    }


    private static synchronized User getUser() {
        if (sUser == null) {
            Console.d(TAG, "User is null, going to try reading it in from SharedPreferences");
            loadUser();
        }

        return sUser;
    }


    public static boolean hasPlayer() {
        return getPlayer() != null;
    }


    private static void loadUser() {
        sUser = new User();
        final SharedPreferences sPreferences = Settings.get(CNAME);

        try {
            // remember that it's possible for the user to not have set a player
            final String playerString = sPreferences.getString(KEY_PLAYER, null);

            if (Utils.validStrings(playerString)) {
                Console.d(TAG, "Read in User's Player from SharedPreferences: " + playerString);
                final JSONObject playerJSON = new JSONObject(playerString);
                sUser.mPlayer = new Player(playerJSON);
            } else {
                Console.d(TAG, "User has no Player saved in SharedPreferences");
            }

            final String regionString = sPreferences.getString(KEY_REGION, null);
            Console.d(TAG, "Read in User's Region from SharedPreferences: " + regionString);

            final JSONObject regionJSON = new JSONObject(regionString);
            sUser.mRegion = new Region(regionJSON);
        } catch (final JSONException e) {
            // this should never happen
            throw new RuntimeException(e);
        }
    }


    private static void saveUser() {
        final User user = getUser();
        final Editor editor = Settings.edit(CNAME);

        if (user.mPlayer != null) {
            final JSONObject playerJSON = user.mPlayer.toJSON();
            final String playerString = playerJSON.toString();
            editor.putString(KEY_PLAYER, playerString);
        }

        final JSONObject regionJSON = user.mRegion.toJSON();
        final String regionString = regionJSON.toString();
        editor.putString(KEY_REGION, regionString);

        editor.apply();
    }


    public static synchronized void setPlayer(final Player player) {
        if (sUser == null) {
            sUser = new User();
        }

        sUser.mPlayer = player;
        saveUser();
    }


    public static synchronized void setRegion(final Region region) {
        if (sUser == null) {
            sUser = new User();
        }

        sUser.mRegion = region;
        saveUser();

        Settings.setRegion(region, false);
    }


    private User() {
        // this constructor is intentionally blank (this prevents it from being accidentally
        // used elsewhere)
    }


    @Override
    public String toString() {
        final String player;

        if (mPlayer == null) {
            player = null;
        } else {
            final JSONObject playerJSON = mPlayer.toJSON();
            player = playerJSON.toString();
        }

        final String region;

        if (mRegion == null) {
            region = null;
        } else {
            final JSONObject regionJSON = mRegion.toJSON();
            region = regionJSON.toString();
        }

        final Context context = App.getContext();
        return context.getString(R.string.player_x_region_y, player, region);
    }


}
