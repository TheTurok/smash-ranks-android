package com.garpr.android.activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.MenuItem;

import com.garpr.android.R;
import com.garpr.android.fragments.PlayersFragment;
import com.garpr.android.fragments.SimpleRegionsFragment;
import com.garpr.android.misc.NonSwipeableViewPager;
import com.garpr.android.misc.OnItemSelectedListener;


public class OnboardingActivity extends BaseActivity implements
        OnItemSelectedListener {


    private static final int ONBOARDING_FRAGMENT_COUNT = 2;
    private static final int ONBOARDING_FRAGMENT_PLAYERS = 1;
    private static final int ONBOARDING_FRAGMENT_REGION = 0;

    private NonSwipeableViewPager mViewPager;
    private PlayersFragment mPlayersFragment;
    private SimpleRegionsFragment mRegionsFragment;




    private void findViews() {
        mViewPager = (NonSwipeableViewPager) findViewById(R.id.activity_onboarding_pager);
    }


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
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");

        findViews();
        prepareViews();
    }


    @Override
    public void onItemSelected() {
        // TODO
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_onboarding_menu_go:
                // TODO
                break;

            case R.id.activity_onboarding_menu_next:
                // TODO
                RankingsActivity.start(this);
                finish();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    private void prepareViews() {
        mViewPager.setAdapter(new OnboardingFragmentAdapter());
    }




    private final class OnboardingFragmentAdapter extends FragmentPagerAdapter {


        private OnboardingFragmentAdapter() {
            super(getSupportFragmentManager());
        }


        @Override
        public int getCount() {
            return ONBOARDING_FRAGMENT_COUNT;
        }


        @Override
        public Fragment getItem(final int position) {
            final Fragment fragment;

            switch (position) {
                case ONBOARDING_FRAGMENT_REGION:
                    if (mRegionsFragment == null) {
                        mRegionsFragment = SimpleRegionsFragment.create();
                    }

                    fragment = mRegionsFragment;
                    break;

                case ONBOARDING_FRAGMENT_PLAYERS:
                    if (mPlayersFragment == null) {
                        mPlayersFragment = PlayersFragment.create();
                    }

                    fragment = mPlayersFragment;
                    break;

                default:
                    // this should never happen
                    throw new RuntimeException();
            }

            return fragment;
        }


    }


}
