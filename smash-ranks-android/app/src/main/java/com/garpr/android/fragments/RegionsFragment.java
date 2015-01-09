package com.garpr.android.fragments;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CheckedTextView;
import android.widget.ImageButton;

import com.garpr.android.R;
import com.garpr.android.data.Regions;
import com.garpr.android.data.Regions.RegionsCallback;
import com.garpr.android.data.Settings;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;
import com.garpr.android.models.Region;

import java.util.ArrayList;
import java.util.Collections;


public class RegionsFragment extends BaseListToolbarFragment {


    private static final String KEY_SELECTED_REGION = "KEY_SELECTED_REGION";
    private static final String TAG = RegionsFragment.class.getSimpleName();


    private ArrayList<Region> mRegions;
    private ImageButton mSave;
    private MenuItem mNext;
    private Region mSelectedRegion;
    private RegionSaveListener mRegionSaveListener;
    private ToolbarNextListener mToolbarNextListener;




    public static RegionsFragment create() {
        return new RegionsFragment();
    }


    private void animateSave() {

    }


    private void disableSave() {
        mSave.setEnabled(false);

        final MarginLayoutParams params = (MarginLayoutParams) mSave.getLayoutParams();
        final int currentMargin = MarginLayoutParamsCompat.getMarginEnd(params);

        final Resources res = getResources();
        final int newMargin = res.getDimensionPixelSize(R.dimen.floating_action_button_disabled);
        final int duration = res.getInteger(android.R.integer.config_shortAnimTime);

        final ValueAnimator animator = ValueAnimator.ofInt(currentMargin, newMargin);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                final int margin = (Integer) animation.getAnimatedValue();
                MarginLayoutParamsCompat.setMarginEnd(params, margin);
                mSave.setLayoutParams(params);
            }
        });

        animator.setDuration(duration);
        animator.start();
    }


    private void enableSave(final boolean animate) {
        mSave.setEnabled(true);

        final MarginLayoutParams params = (MarginLayoutParams) mSave.getLayoutParams();
        final int currentMargin = MarginLayoutParamsCompat.getMarginEnd(params);

        final Resources res = getResources();
        final int newMargin = res.getDimensionPixelSize(R.dimen.floating_action_button_enabled);

        if (animate) {
            final int duration = res.getInteger(android.R.integer.config_shortAnimTime);

            final ValueAnimator animator = ValueAnimator.ofInt(currentMargin, newMargin);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(final ValueAnimator animation) {
                    final int margin = (Integer) animation.getAnimatedValue();
                    MarginLayoutParamsCompat.setMarginEnd(params, margin);
                    mSave.setLayoutParams(params);
                }
            });

            animator.setDuration(duration);
            animator.start();
        } else {
            MarginLayoutParamsCompat.setMarginEnd(params, newMargin);
            mSave.setLayoutParams(params);
        }
    }


    private void fetchRegions() {
        setLoading(true);

        final RegionsCallback callback = new RegionsCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving regions!", e);
                showError();

                try {
                    Analytics.report(TAG).setExtra(e).sendEvent(Constants.NETWORK_EXCEPTION, Constants.REGIONS);
                } catch (final GooglePlayServicesUnavailableException gpsue) {
                    Log.w(TAG, "Unable to report regions exception to analytics", gpsue);
                }
            }


            @Override
            public void response(final ArrayList<Region> list) {
                Collections.sort(list, Region.ALPHABETICAL_ORDER);
                mRegions = list;
                setAdapter(new RegionsAdapter());
            }
        };

        Regions.get(callback);
    }


    private void findToolbarItems() {
        if (mNext == null) {
            final Toolbar toolbar = getToolbar();
            final Menu menu = toolbar.getMenu();
            mNext = menu.findItem(R.id.fragment_regions_menu_next);
        }
    }


    @Override
    protected void findViews() {
        super.findViews();
        final View view = getView();
        mSave = (ImageButton) view.findViewById(R.id.fragment_regions_save);
    }


    @Override
    protected int getContentView() {
        return R.layout.fragment_regions;
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_regions);
    }


    @Override
    protected int getOptionsMenu() {
        final int optionsMenu;

        if (isEmbeddedMode()) {
            optionsMenu = R.menu.fragment_regions;
        } else {
            optionsMenu = 0;
        }

        return optionsMenu;
    }


    public Region getSelectedRegion() {
        return mSelectedRegion;
    }


    private boolean isEmbeddedMode() {
        return mToolbarNextListener != null;
    }


    private boolean isStandaloneMode() {
        return mRegionSaveListener != null;
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mSelectedRegion = savedInstanceState.getParcelable(KEY_SELECTED_REGION);
        }

        if (isStandaloneMode()) {
            final Region region = Settings.getRegion();

            if (mSelectedRegion == null) {
                mSelectedRegion = region;
            } else if (!region.equals(mSelectedRegion)) {
                enableSave(false);
            }
        }

        fetchRegions();
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        if (activity instanceof RegionSaveListener) {
            mRegionSaveListener = (RegionSaveListener) activity;
        }

        if (activity instanceof ToolbarNextListener) {
            mToolbarNextListener = (ToolbarNextListener) activity;
        }

        if (mRegionSaveListener == null && mToolbarNextListener == null) {
            throw new IllegalStateException("Attached Activity must implement a listener");
        } else if (mRegionSaveListener != null && mToolbarNextListener != null) {
            throw new IllegalStateException("Attached Activity can only implement one listener");
        }
    }


    @Override
    protected void onItemClick(final View view, final int position) {
        mSelectedRegion = mRegions.get(position);
        notifyDataSetChanged();

        if (isStandaloneMode()) {
            final Region region = Settings.getRegion();

            if (region.equals(mSelectedRegion)) {
                disableSave();
            } else {
                enableSave(true);
            }
        } else if (isEmbeddedMode()) {
            findToolbarItems();
            mNext.setEnabled(true);
        } else {
            throw new IllegalStateException("Mode is unknown");
        }
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_regions_menu_next:
                mToolbarNextListener.onNextClick();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    public void onRefresh() {
        super.onRefresh();

        if (!isLoading()) {
            Regions.clear();
            fetchRegions();
        }
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSelectedRegion != null) {
            outState.putParcelable(KEY_SELECTED_REGION, mSelectedRegion);
        }
    }


    @Override
    protected void prepareViews() {
        super.prepareViews();
        final Toolbar toolbar = getToolbar();

        if (isStandaloneMode()) {
            final RecyclerView recyclerView = getRecyclerView();
            recyclerView.setClipToPadding(false);

            // TODO
            // adjust the bottom margin / padding so that the action button can properly show

            if (mSelectedRegion != null) {
                enableSave(false);
            }

            mSave.setVisibility(View.VISIBLE);

            mSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    mRegionSaveListener.onRegionSaved();
                }
            });
        } else if (isEmbeddedMode()) {
            toolbar.setTitle(R.string.select_your_region);
            toolbar.setVisibility(View.VISIBLE);
        } else {
            throw new IllegalStateException("Mode is unknown");
        }
    }


    @Override
    protected void setAdapter(final BaseListAdapter adapter) {
        super.setAdapter(adapter);

        if (mSelectedRegion != null && isEmbeddedMode()) {
            findToolbarItems();
            mNext.setEnabled(true);
        }
    }


    @Override
    protected void showError() {
        super.showError();

        if (isStandaloneMode()) {
            mSave.setVisibility(View.GONE);
        }
    }


    @Override
    public String toString() {
        return TAG;
    }




    private final class RegionsAdapter extends BaseListAdapter<ViewHolder> {


        @Override
        public int getItemCount() {
            return mRegions.size();
        }


        @Override
        public long getItemId(final int position) {
            return (long) position;
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Region region = mRegions.get(position);
            holder.mName.setText(region.getName());

            if (region.equals(mSelectedRegion)) {
                holder.mName.setChecked(true);
            } else {
                holder.mName.setChecked(false);
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
            final LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.model_checkable, parent, false);
            view.setOnClickListener(this);
            return new ViewHolder(view);
        }


    }


    private static final class ViewHolder extends RecyclerView.ViewHolder {


        private final CheckedTextView mName;


        private ViewHolder(final View view) {
            super(view);
            mName = (CheckedTextView) view.findViewById(R.id.model_checkable_text);
        }


    }


    public interface RegionSaveListener {


        public void onRegionSaved();


    }


    public interface ToolbarNextListener {


        public void onNextClick();


    }


}
