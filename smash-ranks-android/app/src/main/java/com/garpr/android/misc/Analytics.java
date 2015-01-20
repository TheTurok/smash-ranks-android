package com.garpr.android.misc;


import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.garpr.android.App;
import com.garpr.android.BuildConfig;

import java.util.HashMap;


/**
 * We're using Flurry Analytics: http://www.flurry.com/solutions/analytics
 */
public final class Analytics {


    private static final String DEBUG_API_KEY = "8TMZ969CVDGBBKMFWJ89";
    private static final String RELEASE_API_KEY = "ZWJKZJXQ9XMPNMSGF2SP";




    public static void initialize() {
        final boolean logEnabled;
        final String apiKey;

        if (BuildConfig.DEBUG) {
            logEnabled = true;
            apiKey = DEBUG_API_KEY;
        } else {
            logEnabled = false;
            apiKey = RELEASE_API_KEY;
        }

        final Context context = App.getContext();
        FlurryAgent.setLogEnabled(logEnabled);
        FlurryAgent.init(context, apiKey);
    }


    public static Event report(final Exception e, final String location) {
        return new Event(e, location);
    }


    public static Event report(final String name) {
        return new Event(name);
    }




    public static final class Event {


        private HashMap<String, String> extras;
        private final String name;


        private Event(final Exception e, final String location) {
            this.name = Constants.EXCEPTION;

            String message;

            if (e == null) {
                message = "exception is null";
            } else {
                message = e.getMessage();

                if (!Utils.validStrings(message)) {
                    message = "exception message is null / blank";
                }
            }

            putExtra(Constants.EXCEPTION_MESSAGE, message);

            if (Utils.validStrings(location)) {
                putExtra(Constants.EXCEPTION_LOCATION, location);
            } else {
                throw new IllegalArgumentException("location parameter is malformed");
            }
        }


        private Event(final String name) {
            if (!Utils.validStrings(name)) {
                throw new IllegalArgumentException("name parameter is malformed");
            }

            this.name = name;
        }


        public Event putExtra(final String key, final String value) {
            if (!Utils.validStrings(key, value)) {
                throw new IllegalArgumentException("key / value parameters are malformed");
            }

            if (extras == null) {
                extras = new HashMap<>();
            }

            extras.put(key, value);
            return this;
        }


        public void send() {
            if (extras == null || extras.isEmpty()) {
                FlurryAgent.logEvent(name);
            } else {
                FlurryAgent.logEvent(name, extras);
            }
        }


        @Override
        public String toString() {
            return name;
        }


    }


}
