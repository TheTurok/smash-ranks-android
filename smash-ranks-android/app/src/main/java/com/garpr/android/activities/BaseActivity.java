package com.garpr.android.activities;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.misc.Heartbeat;


/**
 * All Activities should extend from this base class, as it greatly reduces the otherwise
 * necessary boilerplate.
 */
abstract class BaseActivity extends Activity implements Heartbeat {


    private ActionBar mActionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mIsAlive;
    private DrawerLayout mDrawer;
    private ScrollView mDrawerLayout;
    private TextView mDrawerAbout;
    private TextView mDrawerTournaments;




    protected void closeDrawer() {
        mDrawer.closeDrawer(mDrawerLayout);
    }


    private void findViews() {
        mDrawer = (DrawerLayout) findViewById(R.id.navigation_drawer);
        mDrawerAbout = (TextView) findViewById(R.id.navigation_drawer_about);
        mDrawerLayout = (ScrollView) findViewById(R.id.navigation_drawer_layout);
        mDrawerTournaments = (TextView) findViewById(R.id.navigation_drawer_tournaments);
    }


    protected abstract int getContentView();


    protected int getOptionsMenu() {
        return 0;
    }


    private void initializeNavigationDrawer() {
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, R.drawable.icon_drawer,
                R.string.open_drawer, R.string.close_drawer) {
            @Override
            public void onDrawerClosed(final View drawerView) {
                super.onDrawerClosed(drawerView);
                BaseActivity.this.onDrawerClosed();
            }


            @Override
            public void onDrawerOpened(final View drawerView) {
                super.onDrawerOpened(drawerView);
                BaseActivity.this.onDrawerOpened();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(showDrawerIndicator());
        mDrawer.setDrawerListener(mDrawerToggle);

        mDrawerAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                closeDrawer();
            }
        });

        mDrawerTournaments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                closeDrawer();
                TournamentsActivity.start(BaseActivity.this);
            }
        });
    }


    @Override
    public boolean isAlive() {
        return mIsAlive;
    }


    protected boolean isDrawerClosed() {
        return !isDrawerOpen();
    }


    protected boolean isDrawerOpen() {
        return mDrawer.isDrawerOpen(mDrawerLayout);
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
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsAlive = true;
        setContentView(getContentView());
        mActionBar = getActionBar();
        findViews();
        initializeNavigationDrawer();
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


    protected void onDrawerClosed() {
        mActionBar.setTitle(getTitle());
        invalidateOptionsMenu();
    }


    protected void onDrawerOpened() {
        mActionBar.setTitle(R.string.gar_pr);
        invalidateOptionsMenu();
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }


    protected boolean showDrawerIndicator() {
        return true;
    }


}
