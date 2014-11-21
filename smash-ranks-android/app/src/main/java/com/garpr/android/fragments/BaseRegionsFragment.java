package com.garpr.android.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.garpr.android.R;
import com.garpr.android.data.Regions;
import com.garpr.android.data.Regions.RegionsCallback;
import com.garpr.android.misc.OnItemSelectedListener;
import com.garpr.android.models.Region;

import java.util.ArrayList;
import java.util.Collections;


abstract class BaseRegionsFragment extends BaseListFragment {


    private static final String TAG = BaseRegionsFragment.class.getSimpleName();

    private ArrayList<Region> mRegions;
    private OnItemSelectedListener mListener;
    private Region mSelectedRegion;




    protected abstract BaseRegionsAdapter createAdapter();


    private void fetchRegions() {
        setLoading(true);

        final RegionsCallback callback = new RegionsCallback(this) {
            @Override
            public void error(final Exception e) {
                Log.e(TAG, "Exception when retrieving regions!", e);
                showError();
            }


            @Override
            public void response(final ArrayList<Region> list) {
                Collections.sort(list, Region.ALPHABETICAL_ORDER);
                mRegions = list;
                setAdapter(createAdapter());
            }
        };

        Regions.get(callback);
    }


    @Override
    protected String getErrorText() {
        return getString(R.string.error_fetching_regions);
    }


    public Region getSelectedRegion() {
        return mSelectedRegion;
    }


    protected Region getRegion(final int index) {
        return mRegions.get(index);
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchRegions();
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mListener = (OnItemSelectedListener) activity;
    }


    @Override
    protected void onItemClick(final View view, final int position) {
        mSelectedRegion = mRegions.get(position);
        onRegionSelected(view);
        mListener.onItemSelected();
    }


    @Override
    public void onRefresh() {
        super.onRefresh();

        if (!isLoading()) {
            Regions.clear();
            fetchRegions();
        }
    }


    protected abstract void onRegionSelected(final View view);


    protected void setSelectedRegion(final Region region) {
        mSelectedRegion = region;
    }




    protected abstract class BaseRegionsAdapter<T extends RecyclerView.ViewHolder> extends
            BaseListAdapter<T> {


        @Override
        public int getItemCount() {
            return mRegions.size();
        }


    }


}
