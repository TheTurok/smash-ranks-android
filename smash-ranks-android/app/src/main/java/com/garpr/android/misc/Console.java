package com.garpr.android.misc;


import android.util.Log;

import com.garpr.android.models.LogMessage;
import com.garpr.android.models.LogMessage.Level;

import java.lang.ref.WeakReference;
import java.util.LinkedList;


public final class Console {


    private static final int LOG_MESSAGES_MAX_SIZE = 64;
    private static final LinkedList<LogMessage> LOG_MESSAGES;
    private static long sLogMessageIdPointer;
    private static final Object LISTENER_LOCK;
    private static WeakReference<Listener> sListener;




    static {
        LISTENER_LOCK = new Object();
        LOG_MESSAGES = new LinkedList<>();
    }


    private static void add(final Level level, final String tag, final String msg,
            final Throwable tr) {
        final String stackTrace;

        if (tr == null) {
            stackTrace = null;
        } else {
            stackTrace = Log.getStackTraceString(tr);
        }

        synchronized (LOG_MESSAGES) {
            LOG_MESSAGES.addFirst(new LogMessage(level, sLogMessageIdPointer++, tag, msg, stackTrace));

            while (LOG_MESSAGES.size() > LOG_MESSAGES_MAX_SIZE) {
                LOG_MESSAGES.removeLast();
            }
        }

        notifyListener();
    }


    public static void attachListener(final Listener listener) {
        synchronized (LISTENER_LOCK) {
            if (sListener != null && sListener.get() != null) {
                throw new IllegalStateException("Listener already exists");
            }

            sListener = new WeakReference<>(listener);
        }
    }


    public static void clearLogMessages() {
        synchronized (LOG_MESSAGES) {
            LOG_MESSAGES.clear();
        }

        notifyListener();
    }


    public static void d(final String tag, final String msg) {
        Log.d(tag, msg);
        add(Level.DEBUG, tag, msg, null);
    }


    public static void d(final String tag, final String msg, final Throwable tr) {
        Log.d(tag, msg, tr);
        add(Level.DEBUG, tag, msg, tr);
    }


    public static void e(final String tag, final String msg) {
        Log.e(tag, msg);
        add(Level.ERROR, tag, msg, null);
    }


    public static void e(final String tag, final String msg, final Throwable tr) {
        Log.e(tag, msg, tr);
        add(Level.ERROR, tag, msg, tr);
    }


    public static LogMessage getLogMessage(final int position) {
        synchronized (LOG_MESSAGES) {
            return LOG_MESSAGES.get(position);
        }
    }


    public static int getLogMessagesSize() {
        synchronized (LOG_MESSAGES) {
            return LOG_MESSAGES.size();
        }
    }


    public static boolean hasLogMessages() {
        synchronized (LOG_MESSAGES) {
            return !LOG_MESSAGES.isEmpty();
        }
    }


    private static void notifyListener() {
        synchronized (LISTENER_LOCK) {
            if (sListener != null) {
                final Listener listener = sListener.get();

                if (listener == null) {
                    sListener = null;
                } else {
                    listener.onLogMessagesChanged();
                }
            }
        }
    }


    public static void removeListener() {
        synchronized (LISTENER_LOCK) {
            sListener = null;
        }
    }


    public static void w(final String tag, final String msg) {
        Log.w(tag, msg);
        add(Level.WARNING, tag, msg, null);
    }


    public static void w(final String tag, final String msg, final Throwable tr) {
        Log.w(tag, msg, tr);
        add(Level.WARNING, tag, msg, tr);
    }




    public static interface Listener {


        public void onLogMessagesChanged();


    }


}
