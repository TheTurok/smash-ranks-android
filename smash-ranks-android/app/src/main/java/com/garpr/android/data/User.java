package com.garpr.android.data;


import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.garpr.android.models.Player;
import com.garpr.android.models.Region;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.LinkedList;


public final class User {


    private static final String CNAME = User.class.getCanonicalName();
    private static final String KEY_PLAYER = "KEY_PLAYER";
    private static final String KEY_REGION = "KEY_REGION";
    private static final String TAG = User.class.getSimpleName();

    private static LinkedList<WeakReference<OnUserDataChangedListener>> sListeners;
    private static User sUser;

    private Player mPlayer;
    private Region mRegion;




    public static void addListener(final OnUserDataChangedListener listener) {
        final WeakReference<OnUserDataChangedListener> reference = new WeakReference<OnUserDataChangedListener>(listener);

        if (sListeners == null) {
            sListeners = new LinkedList<WeakReference<OnUserDataChangedListener>>();
        }

        sListeners.add(reference);
    }


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


    public static void removeListener(final OnUserDataChangedListener listener) {
        if (sListeners == null || sListeners.isEmpty()) {
            Log.d(TAG, "Went to remove an " + OnUserDataChangedListener.class.getSimpleName()
                    + " but there aren't any");
            return;
        }

        for (int i = 0; i < sListeners.size(); ) {
            final WeakReference<OnUserDataChangedListener> r = sListeners.get(i);
            final OnUserDataChangedListener l = r.get();

            if (l == null || l == listener) {
                Log.d(TAG, "Removing " + OnUserDataChangedListener.class.getSimpleName() +
                        " at index " + i);
                sListeners.remove(i);
            } else {
                ++i;
            }
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


    public static void setPlayer(final Player player) {
        if (!getUser().mPlayer.equals(player)) {
            Log.d(TAG, "Player changed from " + sUser.mPlayer + " to " + player);
            sUser.mPlayer = player;
            saveUser();

            if (sListeners == null || sListeners.isEmpty()) {
                Log.d(TAG, "There are no listeners available");
                return;
            }

            for (int i = 0; i < sListeners.size(); ) {
                final WeakReference<OnUserDataChangedListener> r = sListeners.get(i);
                final OnUserDataChangedListener l = r.get();

                if (l == null) {
                    Log.d(TAG, "Dead " + OnUserDataChangedListener.class.getSimpleName() +
                            " listener found at index " + i);
                    sListeners.remove(i);
                } else {
                    l.onPlayerChanged(sUser.mPlayer);
                    ++i;
                }
            }
        }
    }


    public static void setRegion(final Region region) {
        if (!getUser().mRegion.equals(region)) {
            Log.d(TAG, "Region changed from " + sUser.mRegion + " to " + region);
            sUser.mRegion = region;
            saveUser();

            if (sListeners == null || sListeners.isEmpty()) {
                Log.d(TAG, "There are no listeners available");
                return;
            }

            for (int i = 0; i < sListeners.size(); ) {
                final WeakReference<OnUserDataChangedListener> r = sListeners.get(i);
                final OnUserDataChangedListener l = r.get();

                if (l == null) {
                    Log.d(TAG, "Dead " + OnUserDataChangedListener.class.getSimpleName() +
                            " listener found at index " + i);
                    sListeners.remove(i);
                } else {
                    l.onRegionChanged(sUser.mRegion);
                    ++i;
                }
            }
        }
    }




    public interface OnUserDataChangedListener {


        public void onPlayerChanged(final Player player);


        public void onRegionChanged(final Region region);


    }


}
