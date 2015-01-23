package com.garpr.android.models;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.misc.Utils;


public class LogMessage implements Parcelable {


    private final int priority;
    private final long id;
    private final String message;
    private final String stackTrace;
    private final String tag;
    private final String throwableMessage;




    private void throwIfPriorityIsInvalid() {
        if (!(priority == Log.DEBUG || priority == Log.WARN || priority == Log.ERROR)) {
            throw new IllegalArgumentException("priority must be either Log.DEBUG (" + Log.DEBUG
                    + "), Log.WARN (" + Log.WARN + "), or Log.ERROR (" + Log.ERROR + ")");
        }
    }


    public LogMessage(final int priority, final long id, final String tag, final String message,
            final String stackTrace, final String throwableMessage) {
        this.priority = priority;
        throwIfPriorityIsInvalid();

        this.id = id;
        this.tag = tag;
        this.message = message;
        this.stackTrace = stackTrace;
        this.throwableMessage = throwableMessage;
    }


    private LogMessage(final Parcel source) {
        priority = source.readInt();
        throwIfPriorityIsInvalid();

        id = source.readLong();
        message = source.readString();
        stackTrace = source.readString();
        tag = source.readString();
        throwableMessage = source.readString();
    }


    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof LogMessage) {
            final LogMessage lm = (LogMessage) o;

            if (priority == lm.getPriority() && id == lm.getId() &&
                    message.equals(lm.getMessage()) && tag.equals(lm.getTag())) {
                if (isThrowable() && lm.isThrowable()) {
                    isEqual = stackTrace.equals(lm.getStackTrace()) &&
                            throwableMessage.equals(lm.getThrowableMessage());
                } else if (!isThrowable() && !lm.isThrowable()) {
                    isEqual = true;
                } else {
                    isEqual = false;
                }
            } else {
                isEqual = false;
            }
        } else {
            isEqual = false;
        }

        return isEqual;
    }


    public long getId() {
        return id;
    }


    public String getMessage() {
        return message;
    }


    public int getPriority() {
        return priority;
    }


    public String getStackTrace() {
        return stackTrace;
    }


    public String getTag() {
        return tag;
    }


    public String getThrowableMessage() {
        return throwableMessage;
    }


    public boolean isPriorityDebug() {
        return priority == Log.DEBUG;
    }


    public boolean isPriorityError() {
        return priority == Log.ERROR;
    }


    public boolean isPriorityWarn() {
        return priority == Log.WARN;
    }


    public boolean isThrowable() {
        return Utils.validStrings(stackTrace, throwableMessage);
    }


    @Override
    public String toString() {
        final Context context = App.getContext();
        return context.getString(R.string.x_colon_y, tag, message);
    }




    /*
     * Code necessary for the Android Parcelable interface is below. Read more here:
     * https://developer.android.com/intl/es/reference/android/os/Parcelable.html
     */


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(priority);
        dest.writeLong(id);
        dest.writeString(message);
        dest.writeString(stackTrace);
        dest.writeString(tag);
        dest.writeString(throwableMessage);
    }


    public static final Creator<LogMessage> CREATOR = new Creator<LogMessage>() {
        @Override
        public LogMessage createFromParcel(final Parcel source) {
            return new LogMessage(source);
        }


        @Override
        public LogMessage[] newArray(final int size) {
            return new LogMessage[size];
        }
    };


}
