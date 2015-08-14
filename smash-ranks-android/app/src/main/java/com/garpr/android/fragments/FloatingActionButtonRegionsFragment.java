package com.garpr.android.fragments;


import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.garpr.android.R;
import com.garpr.android.models.Region;
import com.garpr.android.settings.Settings;
import com.garpr.android.views.CheckableItemView;


public class FloatingActionButtonRegionsFragment extends RegionsFragment {


    private static final String TAG = "FloatingActionButtonRegionsFragment";

    private FloatingActionButton mSave;
    private SaveListener mListener;




    public static FloatingActionButtonRegionsFragment create() {
        return new FloatingActionButtonRegionsFragment();
    }


    @Override
    protected void findViews() {
        super.findViews();

        final View view = getView();
        mSave = (FloatingActionButton) view.findViewById(R.id.fragment_regions_save);
    }


    @Override
    protected int getContentView() {
        return R.layout.fragment_floating_action_button_regions;
    }


    @Override
    protected String getFragmentName() {
        return TAG;
    }


    private void measureRecyclerViewBottomOffset() {
        final RecyclerView recyclerView = getRecyclerView();
        recyclerView.setClipToPadding(false);

        final int frameHeight = mFrame.getHeight();
        final int distanceFromTop = mSave.getTop();

        final Resources res = getResources();
        final int rootPadding = res.getDimensionPixelSize(R.dimen.root_padding);
        final int bottom = frameHeight - distanceFromTop + rootPadding;
        final int start = ViewCompat.getPaddingStart(recyclerView);
        final int end = ViewCompat.getPaddingEnd(recyclerView);
        final int top = recyclerView.getPaddingTop();

        ViewCompat.setPaddingRelative(recyclerView, start, top, end, bottom);
        recyclerView.requestLayout();
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Region region = Settings.Region.get();

        if (mSelectedRegion == null || region.equals(mSelectedRegion)) {
            mSelectedRegion = region;
            mSave.hide();
        } else {
            mSave.show();
        }
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mListener = (SaveListener) activity;
    }


    @Override
    public void onClick(final CheckableItemView v) {
        super.onClick(v);
        final Region region = Settings.Region.get();

        if (region.equals(mSelectedRegion)) {
            mSave.hide();
        } else {
            mSave.show();
        }
    }


    @Override
    protected void prepareViews() {
        super.prepareViews();

        final ViewTreeObserver vto = mSave.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                final ViewTreeObserver vto = mSave.getViewTreeObserver();

                if (vto.isAlive()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        vto.removeOnGlobalLayoutListener(this);
                    } else {
                        vto.removeGlobalOnLayoutListener(this);
                    }
                }

                measureRecyclerViewBottomOffset();
            }
        });

        if (mSelectedRegion != null) {
            mSave.show();
        }

        mSave.setVisibility(View.VISIBLE);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mListener.onRegionSaved();
            }
        });
    }


    @Override
    protected void showError() {
        super.showError();
        mSave.setVisibility(View.GONE);
    }




    public interface SaveListener {


        void onRegionSaved();


    }


}
