package com.garpr.android.misc;


import com.google.android.gms.common.ConnectionResult;


public final class GooglePlayServicesUnavailableException extends Exception {


    public static GooglePlayServicesUnavailableException from(final int code) {
        final GooglePlayServicesUnavailableException exception;

        switch (code) {
            case ConnectionResult.SERVICE_DISABLED:
                exception = new GooglePlayServicesUnavailableException("SERVICE_DISABLED");
                break;

            case ConnectionResult.SERVICE_MISSING:
                exception = new GooglePlayServicesUnavailableException("SERVICE_MISSING");
                break;

            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                exception = new GooglePlayServicesUnavailableException("SERVICE_VERSION_UPDATE_REQUIRED");
                break;

            default:
                exception = new GooglePlayServicesUnavailableException(code);
                break;
        }

        return exception;
    }


    private GooglePlayServicesUnavailableException(final int errorCode) {
        super("Google Play Services are unavailable (error code " + errorCode + ")");
    }


    private GooglePlayServicesUnavailableException(final String errorText) {
        super("Google Play Services are unavailable (error " + errorText + ")");
    }


}
