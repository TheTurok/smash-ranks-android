package com.garpr.android.data;


import com.android.volley.VolleyError;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.HeartbeatWithUi;

import java.util.ArrayList;


abstract class CallbackWithUi<T> extends Callback<T> {


    CallbackWithUi(final HeartbeatWithUi heartbeat) {
        super(heartbeat);
    }


    @Override
    HeartbeatWithUi getHeartbeat() {
        return (HeartbeatWithUi) super.getHeartbeat();
    }


    @Override
    public void onErrorResponse(final VolleyError error) {
        responseOnUi(error);
    }


    @Override
    void onItemResponse(final T item) {
        responseOnUi(item);
    }


    @Override
    void onListResponse(final ArrayList<T> list) {
        responseOnUi(list);
    }


    final void responseOnUi(final Exception e) {
        if (isAlive()) {
            final Runnable action = new Runnable() {
                @Override
                public void run() {
                    if (isAlive()) {
                        response(e);
                    } else {
                        warn("Listener died right before running response(Exception)", e);
                    }
                }
            };

            runOnUi(action);
        } else {
            warn("Unable to run response(Exception) on UI as the listener is dead", e);
        }
    }


    final void responseOnUi(final T item) {
        if (isAlive()) {
            final Runnable action = new Runnable() {
                @Override
                public void run() {
                    if (isAlive()) {
                        response(item);
                    } else {
                        warn("Listener died right before running response(T)");
                    }
                }
            };

            runOnUi(action);
        } else {
            warn("Unable to run response(T) on UI as the listener is dead");
        }
    }


    final void responseOnUi(final ArrayList<T> list) {
        if (isAlive()) {
            final Runnable action = new Runnable() {
                @Override
                public void run() {
                    if (isAlive()) {
                        response(list);
                    } else {
                        warn("Listener died right before running response(ArrayList<T>)");
                    }
                }
            };

            runOnUi(action);
        } else {
            warn("Unable to run response(ArrayList<T>) on UI as the listener is dead");
        }
    }


    private void runOnUi(final Runnable action) {
        final HeartbeatWithUi heartbeat = getHeartbeat();

        if (isAlive() && heartbeat != null && heartbeat.isAlive()) {
            heartbeat.runOnUi(action);
        } else {
            warn("Unable to run on UI as the listener is dead");
        }
    }


    private void warn(final String msg) {
        Console.w(getCallbackName(), msg);
    }


    private void warn(final String msg, final Exception e) {
        Console.w(getCallbackName(), msg, e);
    }


}
