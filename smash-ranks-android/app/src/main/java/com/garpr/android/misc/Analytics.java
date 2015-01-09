package com.garpr.android.misc;


import android.content.Context;

import com.garpr.android.App;
import com.garpr.android.BuildConfig;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.google.android.gms.analytics.HitBuilders.ScreenViewBuilder;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.HashMap;
import java.util.Map;


/**
 * Here's the documentation on the various events types supported by Google Analytics:
 * https://developer.android.com/reference/com/google/android/gms/analytics/package-summary.html
 */
public final class Analytics {


    private static final Object sTrackerLock;
    private static final String TRACKING_ID = "UA-57286718-1";

    private static Tracker sTracker;




    static {
        sTrackerLock = new Object();
    }


    private static int getGooglePlayServicesConnectionCode() {
        final Context context = App.getContext();
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
    }


    private static Tracker getTracker() throws GooglePlayServicesUnavailableException {
        throwIfGooglePlayServicesAreUnavailable();

        synchronized (sTrackerLock) {
            if (sTracker == null) {
                final Context context = App.getContext();
                final GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
                sTracker = analytics.newTracker(TRACKING_ID);
            }
        }

        return sTracker;
    }


    public static Event report(final String name) throws GooglePlayServicesUnavailableException {
        throwIfGooglePlayServicesAreUnavailable();
        return new Event(name);
    }


    private static void throwIfGooglePlayServicesAreUnavailable() throws GooglePlayServicesUnavailableException {
        final int code = getGooglePlayServicesConnectionCode();

        if (code != ConnectionResult.SUCCESS) {
            throw GooglePlayServicesUnavailableException.from(code);
        }
    }




    public static final class Event {


        private HashMap<String, String> extras;
        private final String name;


        private Event(final String name) {
            if (!Utils.validStrings(name)) {
                throw new IllegalArgumentException("name parameter is malformed");
            }

            this.name = name;

            if (BuildConfig.DEBUG) {
                putExtra(Constants.DEBUG, Constants.TRUE);
            }
        }


        private boolean hasExtras() {
            return extras != null && !extras.isEmpty();
        }


        private void putExtra(String key, final String value) {
            if (extras == null) {
                extras = new HashMap<>();
            }

            // the documentation states that arbitrary extras must be prefixed with '&'
            // https://developer.android.com/reference/com/google/android/gms/analytics/HitBuilders.HitBuilder.html#set(java.lang.String, java.lang.String)
            if (key.charAt(0) != '&') {
                key = '&' + key;
            }

            extras.put(key, value);
        }


        private void send(final Map<String, String> params) throws GooglePlayServicesUnavailableException {
            final Tracker tracker = getTracker();
            tracker.setScreenName(name);
            tracker.send(params);
        }


        /**
         * @param category
         * Category in which the event will be filed. Example: "Video"
         *
         * @param action
         * Action associated with the event. Example: "Play"
         */
        public void sendEvent(final String category, final String action) throws GooglePlayServicesUnavailableException {
            if (!Utils.validStrings(category, action)) {
                throw new IllegalArgumentException("category / action parameters are malformed");
            }

            final EventBuilder builder = new EventBuilder()
                    .setCategory(category)
                    .setAction(action);

            if (hasExtras()) {
                builder.setAll(extras);
            }

            send(builder.build());
        }


        public void sendScreenView() throws GooglePlayServicesUnavailableException {
            final ScreenViewBuilder builder = new ScreenViewBuilder();

            if (hasExtras()) {
                builder.setAll(extras);
            }

            send(builder.build());
        }


        public Event setExtra(final Exception e) {
            String message;

            if (e == null) {
                message = "exception is null";
            } else {
                message = e.getMessage();

                if (!Utils.validStrings(message)) {
                    message = "exception message is null / blank";
                }
            }

            putExtra(Constants.MESSAGE, message);
            return this;
        }


        public Event setExtra(final String key, final String value) {
            if (!Utils.validStrings(key, value)) {
                throw new IllegalArgumentException("key / value parameters are malformed");
            }

            putExtra(key, value);
            return this;
        }


        @Override
        public String toString() {
            return name;
        }


    }


}
