package com.garpr.android.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.misc.FadeAnimator;
import com.garpr.android.misc.FlexibleSwipeRefreshLayout;


abstract class BaseListActivity extends BaseActivity implements
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
        mErrorView = (TextView) findViewById(R.id.activity_base_list_error);
        mRecyclerView = (RecyclerView) findViewById(R.id.activity_base_list_list);
        mRefreshLayout = (FlexibleSwipeRefreshLayout) findViewById(R.id.activity_base_list_refresh);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_base_list;
    }


    protected String getErrorText() {
        return getString(R.string.error_);
    }


    protected boolean isLoading() {
        return mIsLoading;
    }


    protected void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntentData(getIntent());
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setRecyclerView(mRecyclerView);
    }


    protected void readIntentData(final Intent intent) {
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
        mRefreshLayout.postSetRefreshing(isLoading);
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
