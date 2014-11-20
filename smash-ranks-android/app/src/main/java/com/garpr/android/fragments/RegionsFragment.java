package com.garpr.android.fragments;


import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class RegionsFragment extends BaseListFragment {


    public static RegionsFragment create() {
        return new RegionsFragment();
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    protected void onItemClick(final View view, final int position) {

    }




    private final class RegionsAdapter extends BaseListAdapter<ViewHolder> {


        @Override
        public int getItemCount() {
            return 0;
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
            final LayoutInflater inflater = getLayoutInflater();
            return null;
        }


    }


    private final static class ViewHolder extends RecyclerView.ViewHolder {


        private ViewHolder(final View view) {
            super(view);
        }


    }


}
