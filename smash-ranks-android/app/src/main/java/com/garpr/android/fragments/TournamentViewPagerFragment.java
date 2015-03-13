package com.garpr.android.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.garpr.android.R;
import com.garpr.android.models.TournamentBundle;


public abstract class TournamentViewPagerFragment extends BaseFragment {


    private static final String KEY_BUNDLE = "KEY_BUNDLE";

    private boolean mAttachToRefreshLayout;
    private Listener mListener;
    private RecyclerView mRecyclerView;
    private TournamentBundle mBundle;




    protected static TournamentViewPagerFragment create(final TournamentViewPagerFragment fragment,
            final TournamentBundle bundle) {
        final Bundle arguments = new Bundle();
        arguments.putParcelable(KEY_BUNDLE, bundle);
        fragment.setArguments(arguments);

        return fragment;
    }


    private void findViews() {
        final View view = getView();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_tournament_view_pager_list);
    }


    @Override
    protected int getContentView() {
        return R.layout.fragment_tournament_view_pager;
    }


    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }


    protected TournamentBundle getTournamentBundle() {
        return mBundle;
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        readArguments();
        findViews();
        prepareList();

        if (mAttachToRefreshLayout) {
            mListener.attachToRefreshLayout(mRecyclerView);
            mAttachToRefreshLayout = false;
        }
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mListener = (Listener) activity;
    }


    protected void prepareList() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    private void readArguments() {
        final Bundle arguments = getArguments();
        mBundle = arguments.getParcelable(KEY_BUNDLE);
    }


    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            if (isAdded()) {
                mListener.attachToRefreshLayout(mRecyclerView);
            } else {
                mAttachToRefreshLayout = true;
            }
        } else {
            mAttachToRefreshLayout = false;
        }
    }




    public interface Listener {


        public void attachToRefreshLayout(final RecyclerView recyclerView);


    }


}
