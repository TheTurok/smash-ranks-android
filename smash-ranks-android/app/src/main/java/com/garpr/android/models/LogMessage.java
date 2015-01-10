package com.garpr.android.models;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.misc.Utils;


public class LogMessage implements Parcelable {


    private final Level level;
    private final long id;
    private final String message;
    private final String stackTrace;
    private final String tag;




    public LogMessage(final Level level, final long id, final String tag, final String message,
            final String stackTrace) {
        this.level = level;
        this.id = id;
        this.tag = tag;
        this.message = message;
        this.stackTrace = stackTrace;
    }


    private LogMessage(final Parcel source) {
        level = Level.create(source.readInt());
        id = source.readLong();
        message = source.readString();
        stackTrace = source.readString();
        tag = source.readString();
    }


    @Override
    public boolean equals(final Object o) {
        final boolean isEqual;

        if (this == o) {
            isEqual = true;
        } else if (o instanceof LogMessage) {
            final LogMessage lm = (LogMessage) o;
            isEqual = level.equals(lm.getLevel()) && id == lm.getId() &&
                    message.equals(lm.getMessage()) && tag.equals(lm.getTag()) &&
                    (hasStackTrace() && lm.hasStackTrace() &&
                            stackTrace.equals(lm.getStackTrace()) ||
                            !hasStackTrace() && !lm.hasStackTrace());
        } else {
            isEqual = false;
        }

        return isEqual;
    }


    public Level getLevel() {
        return level;
    }


    public long getId() {
        return id;
    }


    public String getMessage() {
        return message;
    }


    public String getStackTrace() {
        return stackTrace;
    }


    public String getTag() {
        return tag;
    }


    public boolean hasStackTrace() {
        return Utils.validStrings(stackTrace);
    }


    @Override
    public String toString() {
        return tag + ": " + message;
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
        dest.writeInt(level.ordinal());
        dest.writeLong(id);
        dest.writeString(message);
        dest.writeString(stackTrace);
        dest.writeString(tag);
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




    public static enum Level {
        DEBUG, ERROR, WARNING;


        private static Level create(final int ordinal) {
            final Level level;

            if (ordinal == DEBUG.ordinal()) {
                level = DEBUG;
            } else if (ordinal == ERROR.ordinal()) {
                level = ERROR;
            } else if (ordinal == WARNING.ordinal()) {
                level = WARNING;
            } else {
                throw new IllegalArgumentException("Ordinal is invalid: \"" + ordinal + "\"");
            }

            return level;
        }


        @Override
        public String toString() {
            final int resId;

            switch (this) {
                case DEBUG:
                    resId = R.string.debug;
                    break;

                case ERROR:
                    resId = R.string.error;
                    break;

                case WARNING:
                    resId = R.string.warning;
                    break;

                default:
                    throw new IllegalStateException("Level type is invalid");
            }

            final Context context = App.getContext();
            return context.getString(resId);
        }
    }


}
