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


    private static final String TRACKING_ID = "UA-57286718-1";

    private static Tracker sTracker;




    private static int getGooglePlayServicesConnectionCode() {
        final Context context = App.getContext();
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
    }


    private static synchronized Tracker getTracker() throws GooglePlayServicesUnavailableException {
        if (sTracker == null) {
            throwIfGooglePlayServicesAreUnavailable();
            final Context context = App.getContext();
            final GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            sTracker = analytics.newTracker(TRACKING_ID);
        }

        return sTracker;
    }


    public static Event report(final String name) throws GooglePlayServicesUnavailableException {
        return new Event(name);
    }


    private static void throwIfGooglePlayServicesAreUnavailable() throws GooglePlayServicesUnavailableException {
        final int code = getGooglePlayServicesConnectionCode();

        if (code != ConnectionResult.SUCCESS) {
            throw new GooglePlayServicesUnavailableException(code);
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
                extras = new HashMap<>();
                extras.put(Constants.DEBUG, Constants.TRUE);
            }
        }


        private boolean hasExtras() {
            return extras != null && !extras.isEmpty();
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


        public Event set(final String key, final String value) {
            if (!Utils.validStrings(key, value)) {
                throw new IllegalArgumentException("key / value parameters are malformed");
            }

            if (extras == null) {
                extras = new HashMap<>();
            }

            extras.put(key, value);
            return this;
        }


    }


}
