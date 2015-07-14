package com.garpr.android.fragments;


import android.app.Activity;
import android.support.v4.app.Fragment;


public class PlayerActivityDataFragment extends DataFragment {


    private static final String TAG = "PlayerActivityDataFragment";

    private Listeners mListeners;




    public static PlayerActivityDataFragment create() {
        return (PlayerActivityDataFragment) create(new PlayerActivityDataFragment());
    }


    @Override
    protected String getFragmentName() {
        return TAG;
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        if (activity instanceof Listeners) {
            mListeners = (Listeners) activity;
        } else {
            final Fragment fragment = getParentFragment();

            if (fragment instanceof Listeners) {
                mListeners = (Listeners) fragment;
            } else {
                throw new RuntimeException(TAG + " unable to attach to Listeners");
            }
        }
    }


    @Override
    public void onDetach() {
        mListeners = null;
        super.onDetach();
    }




    public interface Listeners {





    }


}
