package com.garpr.android.activities;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import com.garpr.android.App;
import com.garpr.android.misc.Heartbeat;


/**
 * All Activities should extend from this base class, as it greatly reduces the otherwise
 * necessary boilerplate.
 */
abstract class BaseActivity extends Activity implements Heartbeat {


    private boolean mIsAlive;




    protected abstract int getContentView();


    protected int getOptionsMenu() {
        return 0;
    }


    @Override
    public boolean isAlive() {
        return mIsAlive;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsAlive = true;
        setContentView(getContentView());
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final int menuRes = getOptionsMenu();

        if (menuRes != 0) {
            final MenuInflater inflater = getMenuInflater();
            inflater.inflate(menuRes, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsAlive = false;
        App.cancelNetworkRequests(this);
    }


}
