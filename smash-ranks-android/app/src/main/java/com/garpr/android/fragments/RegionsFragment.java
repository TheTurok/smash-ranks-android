package com.garpr.android.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.garpr.android.R;
import com.garpr.android.data.Regions;
import com.garpr.android.data.Regions.RegionsCallback;
import com.garpr.android.data.User;
import com.garpr.android.misc.OnItemSelectedListener;
import com.garpr.android.models.Region;

import java.util.ArrayList;
import java.util.Collections;


public class RegionsFragment extends BaseListFragment {


    private static final String TAG = RegionsFragment.class.getSimpleName();

    private ArrayList<Region> mRegions;
    private OnItemSelectedListener mListener;
    private Region mSelectedRegion;




    public static RegionsFragment create() {
        return new RegionsFragment();
    }


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
                setAdapter(new RegionsAdapter());
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


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSelectedRegion = User.getRegion();
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
        mListener.onItemSelected();

        ((CheckedTextView) view).setChecked(true);
    }


    @Override
    public void onRefresh() {
        super.onRefresh();

        if (!isLoading()) {
            Regions.clear();
            fetchRegions();
        }
    }




    private final class RegionsAdapter extends BaseListAdapter<ViewHolder> {


        @Override
        public int getItemCount() {
            return mRegions.size();
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Region region = mRegions.get(position);
            holder.mName.setText(region.getName());

            final Region sRegion = getSelectedRegion();

            if (sRegion != null && (region == sRegion || region.equals(sRegion))) {
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


    private final static class ViewHolder extends RecyclerView.ViewHolder {


        private final CheckedTextView mName;


        private ViewHolder(final View view) {
            super(view);
            mName = (CheckedTextView) view.findViewById(R.id.model_checkable_name);
        }


    }


}
