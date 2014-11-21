package com.garpr.android.fragments;


import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.models.Region;


public class CheckableRegionsFragment extends BaseRegionsFragment {


    public static CheckableRegionsFragment create() {
        return new CheckableRegionsFragment();
    }


    @Override
    protected CheckableRegionsAdapter createAdapter() {
        return new CheckableRegionsAdapter();
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Region region = Settings.getRegion();
        setSelectedRegion(region);
    }


    @Override
    protected void onRegionSelected(final View view) {
        ((CheckedTextView) view).setChecked(true);
    }




    private final class CheckableRegionsAdapter extends BaseRegionsAdapter<ViewHolder> {


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Region region = getRegion(position);
            holder.mName.setText(region.getName());

            final Region sRegion = getSelectedRegion();

            if (sRegion != null && (region == sRegion || region.equals(sRegion))) {
                onRegionSelected(holder.mName);
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
