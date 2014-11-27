package com.garpr.android.fragments;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.misc.FadeAnimator;
import com.garpr.android.misc.FlexibleSwipeRefreshLayout;


abstract class BaseListFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener {


    private BaseListAdapter mAdapter;
    private boolean mIsLoading;
    private FadeAnimator mErrorAnimator;
    private FadeAnimator mListAnimator;
    private FlexibleSwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private TextView mErrorView;




    private void animateError(final boolean fadeIn) {
        mErrorAnimator = FadeAnimator.animate(mErrorAnimator, mErrorView, fadeIn);
    }


    private void animateList(final boolean fadeIn) {
        mListAnimator = FadeAnimator.animate(mListAnimator, mRecyclerView, fadeIn);
    }


    protected void findViews() {
        final View view = getView();
        mErrorView = (TextView) view.findViewById(R.id.fragment_base_list_error);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_base_list_list);
        mRefreshLayout = (FlexibleSwipeRefreshLayout) view.findViewById(R.id.fragment_base_list_refresh);
    }


    @Override
    protected int getContentView() {
        return R.layout.fragment_base_list;
    }


    protected String getErrorText() {
        return getString(R.string.error_);
    }


    protected boolean isLoading() {
        return mIsLoading;
    }


    protected void notifyDatasetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        readArguments(getArguments());
        findViews();
        prepareViews();
    }


    protected void onItemClick(final View view, final int position) {
        // this method intentionally left blank (children can override)
    }


    @Override
    public void onRefresh() {
        animateError(false);
    }


    protected void prepareViews() {
        mErrorView.setText(getErrorText());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setRecyclerView(mRecyclerView);
    }


    protected void readArguments(final Bundle arguments) {
        // this method intentionally left blank (children can override)
    }


    protected void setAdapter(final BaseListAdapter adapter) {
        animateError(false);
        mAdapter = adapter;
        mRecyclerView.setAdapter(mAdapter);
        animateList(true);
        setLoading(false);
    }


    protected void setLoading(final boolean isLoading) {
        mIsLoading = isLoading;

        // normally we'd just do mRefreshLayout.setRefreshing(isLoading) here, but unfortunately
        // there's a bug in the appcompat library that requires doing it this way:
        // https://code.google.com/p/android/issues/detail?id=77712
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(isLoading);
            }
        });
    }


    protected void showError() {
        setLoading(false);
        animateList(false);
        animateError(true);
    }




    protected abstract class BaseListAdapter<T extends RecyclerView.ViewHolder> extends
            RecyclerView.Adapter<T> implements View.OnClickListener {


        @Override
        public void onClick(final View v) {
            final int position = mRecyclerView.getChildPosition(v);
            onItemClick(v, position);
        }


    }


}
