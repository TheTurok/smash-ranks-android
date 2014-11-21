package com.garpr.android.fragments;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.models.Region;


public class SimpleRegionsFragment extends BaseRegionsFragment {


    public static SimpleRegionsFragment create() {
        return new SimpleRegionsFragment();
    }


    @Override
    protected SimpleRegionsAdapter createAdapter() {
        return new SimpleRegionsAdapter();
    }


    @Override
    protected void onRegionSelected(final View view) {
        // TODO
        // highlight the view or something
    }




    private final class SimpleRegionsAdapter extends BaseRegionsAdapter<ViewHolder> {


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Region region = getRegion(position);
            holder.mName.setText(region.getName());

            final Region selectedRegion = getSelectedRegion();

            if (selectedRegion != null && (region == selectedRegion || region.equals(selectedRegion))) {
                onRegionSelected(holder.mName);
            } else {
                // TODO
                // clear the highlight or something
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
            final LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.model_simple, parent, false);
            view.setOnClickListener(this);
            return new ViewHolder(view);
        }


    }


    private final static class ViewHolder extends RecyclerView.ViewHolder {


        private final TextView mName;


        private ViewHolder(final View view) {
            super(view);
            mName = (TextView) view.findViewById(R.id.model_simple_name);
        }


    }


}
