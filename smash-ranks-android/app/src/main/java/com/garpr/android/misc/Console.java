package com.garpr.android.misc;


import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.garpr.android.models.LogMessage;

import java.lang.ref.WeakReference;
import java.util.LinkedList;


public final class Console {


    private static final int LOG_MESSAGES_MAX_SIZE = 96;
    private static final LinkedList<LogMessage> LOG_MESSAGES;
    private static final LinkedList<WeakReference<Listener>> LOG_LISTENERS;
    private static long sLogMessageIdPointer;




    static {
        LOG_LISTENERS = new LinkedList<>();
        LOG_MESSAGES = new LinkedList<>();
    }


    private static void add(final int priority, final String tag, final String msg,
            final Throwable tr) {
        final String stackTrace;
        final String throwableMessage;

        Crashlytics.getInstance().core.log(priority, tag, msg);

        if (tr == null) {
            stackTrace = null;
            throwableMessage = null;
        } else {
            Crashlytics.getInstance().core.logException(tr);
            stackTrace = Log.getStackTraceString(tr);
            throwableMessage = tr.getMessage();
        }

        synchronized (LOG_MESSAGES) {
            LOG_MESSAGES.addFirst(new LogMessage(priority, sLogMessageIdPointer++, tag, msg,
                    stackTrace, throwableMessage));

            while (LOG_MESSAGES.size() > LOG_MESSAGES_MAX_SIZE) {
                LOG_MESSAGES.removeLast();
            }
        }

        notifyListeners();
    }


    public static void attachListener(final Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener can't be null");
        }

        synchronized (LOG_LISTENERS) {
            boolean listenerExists = false;

            for (int i = 0; i < LOG_LISTENERS.size() && !listenerExists; ) {
                final WeakReference<Listener> wrl = LOG_LISTENERS.get(i);

                if (wrl == null) {
                    LOG_LISTENERS.remove(i);
                } else {
                    final Listener l = wrl.get();

                    if (l == null) {
                        LOG_LISTENERS.remove(i);
                    } else if (l == listener) {
                        listenerExists = true;
                    } else {
                        ++i;
                    }
                }
            }

            if (!listenerExists) {
                final WeakReference<Listener> wrl = new WeakReference<>(listener);
                LOG_LISTENERS.add(wrl);
            }
        }
    }


    public static void clearLogMessages() {
        synchronized (LOG_MESSAGES) {
            LOG_MESSAGES.clear();
        }

        notifyListeners();
    }


    public static void d(final String tag, final String msg) {
        d(tag, msg, null);
    }


    public static void d(final String tag, final String msg, final Throwable tr) {
        add(Log.DEBUG, tag, msg, tr);
    }


    public static void detachListener(final Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener can't be null");
        }

        synchronized (LOG_LISTENERS) {
            if (LOG_LISTENERS.isEmpty()) {
                final WeakReference<Listener> wrl = new WeakReference<>(listener);
                LOG_LISTENERS.add(wrl);
            } else {
                for (int i = 0; i < LOG_LISTENERS.size(); ) {
                    final WeakReference<Listener> wrl = LOG_LISTENERS.get(i);

                    if (wrl == null) {
                        LOG_LISTENERS.remove(i);
                    } else {
                        final Listener l = wrl.get();

                        if (l == null || l == listener) {
                            LOG_LISTENERS.remove(i);
                        } else {
                            ++i;
                        }
                    }
                }
            }
        }
    }


    public static void e(final String tag, final String msg) {
        e(tag, msg, null);
    }


    public static void e(final String tag, final String msg, final Throwable tr) {
        add(Log.ERROR, tag, msg, tr);
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


    private static void notifyListeners() {
        synchronized (LOG_LISTENERS) {
            if (LOG_LISTENERS.isEmpty()) {
                return;
            }

            for (int i = 0; i < LOG_LISTENERS.size(); ) {
                final WeakReference<Listener> wrl = LOG_LISTENERS.get(i);

                if (wrl == null) {
                    LOG_LISTENERS.remove(i);
                } else {
                    final Listener l = wrl.get();

                    if (l == null) {
                        LOG_LISTENERS.remove(i);
                    } else {
                        l.onLogMessagesChanged();
                        ++i;
                    }
                }
            }
        }
    }


    public static void w(final String tag, final String msg) {
        w(tag, msg, null);
    }


    public static void w(final String tag, final String msg, final Throwable tr) {
        add(Log.WARN, tag, msg, tr);
    }




    public interface Listener {


        void onLogMessagesChanged();


    }


}
