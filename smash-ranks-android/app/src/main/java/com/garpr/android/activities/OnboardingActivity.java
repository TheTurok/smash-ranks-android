package com.garpr.android.activities;


import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.data.User;
import com.garpr.android.fragments.PlayersFragment;
import com.garpr.android.fragments.RegionsFragment;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Analytics.Event;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.NonSwipeableViewPager;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;


public class OnboardingActivity extends BaseActivity implements
        PlayersFragment.Listeners,
        RegionsFragment.ToolbarNextListener {


    private static final int ONBOARDING_FRAGMENT_COUNT = 2;
    private static final int ONBOARDING_FRAGMENT_PLAYERS = 1;
    private static final int ONBOARDING_FRAGMENT_REGIONS = 0;
    private static final String CNAME = "com.garpr.android.activities.OnboardingActivity";
    private static final String KEY_ONBOARDING_COMPLETE = "KEY_ONBOARDING_COMPLETE";
    private static final String KEY_SELECTED_REGION = "KEY_SELECTED_REGION";
    private static final String TAG = "OnboardingActivity";

    private boolean mReportToAnalytics;
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
        final Event event = Analytics.report(Constants.ONBOARDING)
                .putExtra(Constants.REGION, mSelectedRegion.getName());

        if (savePlayer) {
            final Player player = mPlayersFragment.getSelectedPlayer();
            User.setPlayer(player);

            event.putExtra(Constants.ONBOARDING_STATUS, Constants.COMPLETED)
                    .putExtra(Constants.PLAYER, player.getName());
        } else {
            event.putExtra(Constants.ONBOARDING_STATUS, Constants.SKIPPED);
        }

        event.send();

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
        switch (mPager.getCurrentItem()) {
            case ONBOARDING_FRAGMENT_REGIONS:
                final Region region = mRegionsFragment.getSelectedRegion();

                if (!region.equals(mSelectedRegion)) {
                    mSelectedRegion = region;
                    User.setRegion(mSelectedRegion);
                    mPlayersFragment.refresh();
                }

                mPager.setCurrentItem(ONBOARDING_FRAGMENT_PLAYERS, true);
                break;

            default:
                // this should never happen
                throw new RuntimeException();
        }
    }


    @Override
    public void onBackPressed() {
        if (mPager == null || mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            switch (mPager.getCurrentItem()) {
                case ONBOARDING_FRAGMENT_PLAYERS:
                    if (!mPlayersFragment.onBackPressed()) {
                        mPager.setCurrentItem(ONBOARDING_FRAGMENT_REGIONS, true);
                    }
                    break;

                default:
                    // this should never happen
                    throw new RuntimeException();
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
        mReportToAnalytics = true;

        if (onboardingCompleted()) {
            Crashlytics.setBool(Constants.SKIPPED_ONBOARDING, true);

            mReportToAnalytics = false;
            RankingsActivity.start(this);
            finish();
        } else {
            Crashlytics.setBool(Constants.SKIPPED_ONBOARDING, false);

            findViews();
            prepareViews();
        }
    }


    @Override
    public void onGoClick() {
        final Player player = mPlayersFragment.getSelectedPlayer();

        new MaterialDialog.Builder(this)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(final MaterialDialog materialDialog) {
                        finishOnboarding(true);
                    }
                })
                .content(R.string.youre_x_from_y_ready, player.getName(), mSelectedRegion.getName())
                .negativeText(R.string.cancel)
                .positiveText(R.string.lets_go)
                .show();
    }


    @Override
    public void onNextClick() {
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
        new MaterialDialog.Builder(this)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(final MaterialDialog materialDialog) {
                        finishOnboarding(false);
                    }
                })
                .content(R.string.are_you_sure_you_dont_want_to_select_your_tag)
                .negativeText(R.string.cancel)
                .positiveText(R.string.yes)
                .show();
    }


    private void prepareViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isOrientationLandscape()) {
                applyStatusBarHeightAsHeight(mTop);
                mTop.setVisibility(View.VISIBLE);
            } else {
                applyStatusBarHeightAsTopPadding(mTop);
            }
        }

        mPager.setAdapter(new OnboardingFragmentAdapter());
    }


    @Override
    protected boolean reportToAnalytics() {
        return mReportToAnalytics;
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
                    fragment = RegionsFragment.create();
                    break;

                case ONBOARDING_FRAGMENT_PLAYERS:
                    fragment = PlayersFragment.create();
                    break;

                default:
                    // this should never happen
                    throw new RuntimeException();
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
