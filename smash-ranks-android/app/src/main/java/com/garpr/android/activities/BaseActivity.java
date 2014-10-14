package com.garpr.android.activities;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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


    /**
     * This method's code taken from the Android documentation:
     * https://developer.android.com/training/implementing-navigation/ancestral.html
     */
    private void navigateUp() {
        final Intent upIntent = NavUtils.getParentActivityIntent(this);

        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            NavUtils.navigateUpTo(this, upIntent);
        }
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsAlive = true;
        setContentView(getContentView());

        if (showHomeAsUpEnabled()) {
            final ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    protected boolean showHomeAsUpEnabled() {
        return false;
    }


}
