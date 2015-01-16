package com.garpr.android.data;


abstract class AsyncRunnable implements Runnable {


    abstract String getAsyncRunnableName();


    void start() {
        final Thread thread = new Thread(this);
        thread.start();
    }


}
