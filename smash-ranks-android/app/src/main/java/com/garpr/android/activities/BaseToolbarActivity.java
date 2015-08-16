package com.garpr.android.activities;


import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;
import com.garpr.android.settings.Settings;
import com.garpr.android.settings.Settings.User;
import com.garpr.android.views.NavigationHeaderView;


public abstract class BaseToolbarActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener {


    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationHeaderView mNavigationHeaderView;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;




    protected void closeDrawer() {
        mDrawerLayout.closeDrawer(mNavigationView);
    }


    private void findViews() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationHeaderView = (NavigationHeaderView) findViewById(R.id.navigation_header_view);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }


    protected int getOptionsMenu() {
        return 0;
    }


    protected View getSelectedDrawerView(final TextView about, final TextView rankings,
            final TextView settings, final TextView tournaments) {
        return null;
    }


    protected Toolbar getToolbar() {
        return mToolbar;
    }


    private void initializeNavigationDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.open_drawer, R.string.close_drawer) {
            @Override
            public void onDrawerClosed(final View drawerView) {
                super.onDrawerClosed(drawerView);
                BaseToolbarActivity.this.onDrawerClosed();
            }


            @Override
            public void onDrawerOpened(final View drawerView) {
                super.onDrawerOpened(drawerView);
                BaseToolbarActivity.this.onDrawerOpened();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mNavigationView.setNavigationItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            applyStatusBarHeightAsTopMargin(mDrawerContents, false);
            mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.gray_dark));
        }

        if (User.hasPlayer()) {
            final Player player = User.Player.get();
            mDrawerUserName.setText(player.getName());
        } else {
            mDrawerUserName.setVisibility(View.GONE);
        }

        updateDrawerRegion();

        if (showDrawerIndicator()) {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        } else {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mToolbar.setNavigationIcon(R.drawable.ic_back);

            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    navigateUp();
                }
            });
        }

        mDrawerBuffer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                return true;
            }
        });

        mDrawerAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                closeDrawer();
                AboutActivity.start(BaseToolbarActivity.this);
            }
        });

        mDrawerRankings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                closeDrawer();
                RankingsActivity.start(BaseToolbarActivity.this);
            }
        });

        mDrawerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                closeDrawer();
                SettingsActivity.start(BaseToolbarActivity.this);
            }
        });

        mDrawerTournaments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                closeDrawer();
                TournamentsActivity.start(BaseToolbarActivity.this);
            }
        });

        final View view = getSelectedDrawerView(mDrawerAbout, mDrawerRankings, mDrawerSettings,
                mDrawerTournaments);

        if (view != null) {
            view.setBackgroundColor(getResources().getColor(R.color.overlay_bright));
            view.setSelected(true);
        }
    }


    protected boolean isDrawerClosed() {
        return !isDrawerOpen();
    }


    protected boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mNavigationView);
    }


    protected boolean isDrawerVisible() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerVisible(mNavigationView);
    }


    @Override
    public void onBackPressed() {
        if (isDrawerVisible()) {
            closeDrawer();
        } else {
            super.onBackPressed();
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
        findViews();

        setSupportActionBar(mToolbar);
        initializeNavigationDrawer();
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final int menuRes = getOptionsMenu();

        if (menuRes != 0) {
            final MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(menuRes, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }


    protected void onDrawerClosed() {
        // this method intentionally left blank (children can override)
    }


    protected void onDrawerOpened() {
        // this method intentionally left blank (children can override)
    }


    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_view_menu_about:
                AboutActivity.start(this);
                break;

            case R.id.navigation_view_menu_rankings:
                RankingsActivity.start(this);
                break;

            case R.id.navigation_view_menu_settings:
                SettingsActivity.start(this);
                break;

            case R.id.navigation_view_menu_tournaments:
                TournamentsActivity.start(this);
                break;
        }

        return false;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final boolean actionConsumed;

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            actionConsumed = true;
        } else {
            actionConsumed = super.onOptionsItemSelected(item);
        }

        return actionConsumed;
    }


    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }


    @Override
    public void onRegionChanged(final Region region) {
        super.onRegionChanged(region);
        updateDrawerRegion();
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateDrawerRegion();
    }


    protected boolean showDrawerIndicator() {
        return true;
    }


    private void updateDrawerRegion() {
        final Region userRegion = User.Region.get();
        final Region settingsRegion = Settings.Region.get();
        final String regionText;

        if (userRegion.equals(settingsRegion)) {
            regionText = userRegion.getName();
        } else {
            regionText = getString(R.string.x_viewing_y, userRegion.getName(), settingsRegion.getName());
        }

        mDrawerUserRegion.setText(regionText);
    }


}
