package com.garpr.android.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.garpr.android.App;
import com.garpr.android.data.User;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.models.Player;
import com.garpr.android.models.Region;


public abstract class BaseFragment extends Fragment implements
        Heartbeat,
        User.OnUserDataChangedListener {


    private boolean mIsAlive;




    protected abstract int getContentView();


    protected LayoutInflater getLayoutInflater() {
        return getActivity().getLayoutInflater();
    }


    @Override
    public boolean isAlive() {
        return mIsAlive;
    }


    protected boolean listenForUserChanges() {
        return false;
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (listenForUserChanges()) {
            User.addListener(this);
        }
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mIsAlive = true;
        return inflater.inflate(getContentView(), container, false);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIsAlive = false;
        App.cancelNetworkRequests(this);

        if (listenForUserChanges()) {
            User.removeListener(this);
        }
    }


    @Override
    public void onPlayerChanged(final Player player) {
        // this method intentionally left blank (children can override)
    }


    @Override
    public void onRegionChanged(final Region region) {
        // this method intentionally left blank (children can override)
    }


}
