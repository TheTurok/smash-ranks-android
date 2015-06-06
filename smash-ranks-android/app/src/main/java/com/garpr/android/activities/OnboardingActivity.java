package com.garpr.android.activities;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.User;
import com.garpr.android.fragments.PlayersFragment;
import com.garpr.android.fragments.RegionsFragment;
import com.garpr.android.fragments.ToolbarRegionsFragment;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;
import com.garpr.android.views.NonSwipeableViewPager;


public class OnboardingActivity extends BaseActivity implements PlayersFragment.Listeners,
        ToolbarRegionsFragment.NextListener {


    private static final int ONBOARDING_FRAGMENT_COUNT = 2;
    private static final int ONBOARDING_FRAGMENT_PLAYERS = 1;
    private static final int ONBOARDING_FRAGMENT_REGIONS = 0;
    private static final String CNAME = "com.garpr.android.activities.OnboardingActivity";
    private static final String KEY_ONBOARDING_COMPLETE = "KEY_ONBOARDING_COMPLETE";
    private static final String KEY_SELECTED_REGION = "KEY_SELECTED_REGION";
    private static final String TAG = "OnboardingActivity";

    private NonSwipeableViewPager mPager;
    private PlayersFragment mPlayersFragment;
    private Region mSelectedRegion;
    private RegionsFragment mRegionsFragment;
    private View mTop;




    private void findViews() {
        mPager = (NonSwipeableViewPager) findViewById(R.id.activity_onboarding_pager);
        mTop = findViewById(R.id.activity_onboarding_top);
    }


    private void finishOnboarding(final boolean savePlayer) {
        if (savePlayer) {
            User.setPlayer(mPlayersFragment.getSelectedPlayer());
        }

        final Editor editor = Settings.edit(CNAME);
        editor.putBoolean(KEY_ONBOARDING_COMPLETE, true);
        editor.apply();

        RankingsActivity.start(this);
        finish();
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_onboarding;
    }


    private void nextOnboardingStep() {
        final int currentPagerItem = mPager.getCurrentItem();

        if (currentPagerItem == ONBOARDING_FRAGMENT_REGIONS) {
            final Region region = mRegionsFragment.getSelectedRegion();

            if (!region.equals(mSelectedRegion)) {
                mSelectedRegion = region;
                User.setRegion(mSelectedRegion);
                mPlayersFragment.refresh();
            }

            mPager.setCurrentItem(ONBOARDING_FRAGMENT_PLAYERS, true);
        } else {
            Console.w(TAG, "Illegal currentPagerItem in nextOnboardingStep(): " + currentPagerItem);
        }
    }


    @Override
    public void onBackPressed() {
        if (mPager == null || mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            final int currentPagerItem = mPager.getCurrentItem();

            if (currentPagerItem == ONBOARDING_FRAGMENT_PLAYERS) {
                if (!mPlayersFragment.onBackPressed()) {
                    mPager.setCurrentItem(ONBOARDING_FRAGMENT_REGIONS, true);
                }
            } else {
                Console.w(TAG, "Illegal currentPagerItem in onBackPressed(): " + currentPagerItem);
            }
        }
    }


    private boolean onboardingCompleted() {
        final SharedPreferences sPreferences = Settings.get(CNAME);
        return sPreferences.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final boolean onboardingCompleted = onboardingCompleted();
        Crashlytics.getInstance().core.setBool(Constants.ONBOARDING_ALREADY_COMPLETED, onboardingCompleted);

        if (onboardingCompleted) {
            RankingsActivity.start(this);
            finish();
        } else {
            findViews();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                prepareStatusBar();
            }

            mPager.setAdapter(new OnboardingFragmentAdapter());
        }
    }


    @Override
    public void onGoClick() {
        final Player player = mPlayersFragment.getSelectedPlayer();

        new AlertDialog.Builder(this)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                })
                .setMessage(getString(R.string.youre_x_from_y_ready, player.getName(),
                        mSelectedRegion.getName()))
                .setPositiveButton(R.string.lets_go, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                        finishOnboarding(true);
                    }
                })
                .show();
    }


    @Override
    public void onRegionNextClick() {
        nextOnboardingStep();
    }


    @Override
    @SuppressWarnings("NullableProblems")
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (!savedInstanceState.isEmpty()) {
            mSelectedRegion = savedInstanceState.getParcelable(KEY_SELECTED_REGION);
        }
    }


    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSelectedRegion != null) {
            outState.putParcelable(KEY_SELECTED_REGION, mSelectedRegion);
        }
    }


    @Override
    public void onSkipClick() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.are_you_sure_you_dont_want_to_select_your_tag)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                        finishOnboarding(false);
                    }
                })
                .show();
    }


    private void prepareStatusBar() {
        if (isOrientationLandscape()) {
            applyStatusBarHeightAsHeight(mTop);
            mTop.setVisibility(View.VISIBLE);
        } else {
            applyStatusBarHeightAsTopPadding(mTop);
        }
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
                case ONBOARDING_FRAGMENT_REGIONS:
                    fragment = ToolbarRegionsFragment.create();
                    break;

                case ONBOARDING_FRAGMENT_PLAYERS:
                    fragment = PlayersFragment.create();
                    break;

                default:
                    // this should never happen
                    throw new RuntimeException("Invalid position: " + position);
            }

            return fragment;
        }


        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            final Object item = super.instantiateItem(container, position);

            switch (position) {
                case ONBOARDING_FRAGMENT_PLAYERS:
                    mPlayersFragment = (PlayersFragment) item;
                    break;

                case ONBOARDING_FRAGMENT_REGIONS:
                    mRegionsFragment = (RegionsFragment) item;
                    break;
            }

            return item;
        }


    }


}
