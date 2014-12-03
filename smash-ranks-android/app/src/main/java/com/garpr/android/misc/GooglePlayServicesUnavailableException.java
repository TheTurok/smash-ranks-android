package com.garpr.android.misc;


public final class GooglePlayServicesUnavailableException extends Exception {


    private final int mErrorCode;


    public GooglePlayServicesUnavailableException(final int errorCode) {
        super("Google Play Services are unavailable (error code " + errorCode + ")");
        mErrorCode = errorCode;
    }


    public int getErrorCode() {
        return mErrorCode;
    }


}
