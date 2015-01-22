package com.garpr.android.data;


import com.garpr.android.misc.Console;


abstract class AsyncRunnable implements Runnable {


    abstract String getAsyncRunnableName();


    void start() {
        Console.d(getAsyncRunnableName(), "Starting thread");

        final Thread thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }


}
