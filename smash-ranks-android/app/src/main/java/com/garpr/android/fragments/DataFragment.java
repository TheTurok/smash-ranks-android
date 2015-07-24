package com.garpr.android.fragments;


import android.app.Activity;
import android.support.v4.app.Fragment;

import com.garpr.android.misc.Heartbeat;


public abstract class DataFragment extends Fragment implements Heartbeat {


    private boolean mIsAlive;




    protected static DataFragment create(final DataFragment fragment) {
        fragment.setRetainInstance(true);
        return fragment;
    }


    protected abstract String getFragmentName();


    @Override
    public boolean isAlive() {
        return mIsAlive;
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mIsAlive = true;
    }


    @Override
    public void onDetach() {
        mIsAlive = false;
        super.onDetach();
    }


    @Override
    public String toString() {
        return getFragmentName();
    }


}
