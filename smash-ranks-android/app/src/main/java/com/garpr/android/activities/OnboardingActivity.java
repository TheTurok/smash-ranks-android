package com.garpr.android.activities;


import android.view.MenuItem;

import com.garpr.android.R;


public class OnboardingActivity extends BaseActivity {


    @Override
    protected boolean isNavigationDrawerEnabled() {
        return false;
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_onboarding;
    }


    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_onboarding;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_onboarding_menu_go:
                // TODO
                break;

            case R.id.activity_onboarding_menu_next:
                RankingsActivity.start(this);
                finish();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


}
