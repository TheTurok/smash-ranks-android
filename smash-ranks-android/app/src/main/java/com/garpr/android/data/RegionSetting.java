package com.garpr.android.data;


import com.crashlytics.android.Crashlytics;
import com.garpr.android.User;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Region;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.LinkedList;


public class RegionSetting extends Setting<Region> {


    private static final String TAG = "RegionSetting";

    private final JSONSetting mJSONSetting;
    private final LinkedList<Attachment> mListeners;




    RegionSetting(final String key) {
        super(key);
        mJSONSetting = new JSONSetting(key);
        mListeners = new LinkedList<>();
    }


    public void attachListener(final Heartbeat heartbeat, final RegionListener listener) {
        if (Utils.areAnyObjectsNull(heartbeat, listener)) {
            throw new RuntimeException();
        }

        synchronized (mListeners) {
            boolean attachListener = true;

            for (int i = 0; i < mListeners.size(); ) {
                final Attachment attachment = mListeners.get(i);

                if (attachment.isAlive()) {
                    if (attachment.hasListener(listener)) {
                        attachListener = false;
                    }

                    ++i;
                } else {
                    mListeners.remove(i);
                }
            }

            if (!heartbeat.isAlive()) {
                Console.w(TAG, "Attempted to attach a listener with a dead heartbeat");
                return;
            }

            if (attachListener) {
                mListeners.add(new Attachment(heartbeat, listener));
            }

            Console.d(TAG, "There are now " + mListeners.size() + " region listener(s)");
        }
    }


    public void detachListener(final RegionListener listener) {
        synchronized (mListeners) {
            for (int i = 0; i < mListeners.size(); ) {
                final Attachment attachment = mListeners.get(i);

                if (!attachment.isAlive() || attachment.hasListener(listener)) {
                    mListeners.remove(i);
                } else {
                    ++i;
                }
            }

            Console.d(TAG, "There are now " + mListeners.size() + " region listener(s)");
        }
    }


    @Override
    public Region get() {
        final JSONObject json = mJSONSetting.get();
        final Region region;

        if (json == null) {
            region = User.getRegion();
            set(region);
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
        if (newValue == null) {
            throw new IllegalArgumentException("newValue can't be null");
        }

        final JSONObject json = newValue.toJSON();
        Console.d(TAG, "Region is now " + json.toString());
        Crashlytics.getInstance().core.setString(Constants.REGION, json.toString());
        mJSONSetting.set(json);
    }


    public void set(final Region newValue, final boolean notifyListeners) {
        set(newValue);

        if (notifyListeners) {
            synchronized (mListeners) {
                for (int i = 0; i < mListeners.size(); ) {
                    final Attachment attachment = mListeners.get(i);

                    if (attachment.isAlive()) {
                        attachment.onRegionChanged(newValue);
                        ++i;
                    } else {
                        mListeners.remove(i);
                    }
                }
            }
        }
    }




    private final static class Attachment implements Heartbeat, RegionListener {


        private final WeakReference<Heartbeat> mHeartbeat;
        private final WeakReference<RegionListener> mListener;


        private Attachment(final Heartbeat heartbeat, final RegionListener listener) {
            mHeartbeat = new WeakReference<>(heartbeat);
            mListener = new WeakReference<>(listener);
        }


        private boolean hasListener(final RegionListener listener) {
            return mListener.get() == listener;
        }


        @Override
        public boolean isAlive() {
            final Heartbeat heartbeat = mHeartbeat.get();
            return heartbeat != null && heartbeat.isAlive() && mListener.get() != null;
        }


        @Override
        public void onRegionChanged(final Region region) {
            if (!isAlive()) {
                return;
            }

            final RegionListener listener = mListener.get();

            if (listener != null) {
                listener.onRegionChanged(region);
            }
        }


    }


    public interface RegionListener {


        void onRegionChanged(final Region region);


    }


}
