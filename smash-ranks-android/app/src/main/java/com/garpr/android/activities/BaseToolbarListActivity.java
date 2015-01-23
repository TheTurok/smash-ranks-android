package com.garpr.android.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.misc.BaseListAdapter;
import com.garpr.android.misc.FlexibleSwipeRefreshLayout;


abstract class BaseToolbarListActivity extends BaseToolbarActivity implements
        BaseListAdapter.Listener,
        SwipeRefreshLayout.OnRefreshListener {


    private BaseListAdapter mAdapter;
    private boolean mIsLoading;
    private FlexibleSwipeRefreshLayout mRefreshLayout;
    private LinearLayout mErrorView;
    private RecyclerView mRecyclerView;
    private TextView mErrorLine;




    protected void findViews() {
        mErrorLine = (TextView) findViewById(R.id.activity_base_list_error_line);
        mErrorView = (LinearLayout) findViewById(R.id.activity_base_list_error);
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


    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }


    protected boolean isLoading() {
        return mIsLoading;
    }


    protected void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.dataSetChanged();
        }
    }


    protected void notifyItemChanged(final int position) {
        if (mAdapter != null) {
            mAdapter.itemChanged(position);
        }
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntentData(getIntent());
        findViews();
        prepareViews();
    }


    @Override
    public void onItemClick(final View view, final int position) {
        // this method intentionally left blank (children can override)
    }


    @Override
    public boolean onItemLongClick(final View view, final int position) {
        // this method intentionally left blank (children can override)
        return false;
    }


    @Override
    public void onRefresh() {
        mErrorView.setVisibility(View.GONE);
    }


    protected void prepareViews() {
        mErrorLine.setText(getErrorText());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setRecyclerView(mRecyclerView);
    }


    protected void readIntentData(final Intent intent) {
        // this method intentionally left blank (children can override)
    }


    protected void setAdapter(final BaseListAdapter adapter) {
        mErrorView.setVisibility(View.GONE);
        mAdapter = adapter;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        setLoading(false);
    }


    protected void setLoading(final boolean isLoading) {
        mIsLoading = isLoading;
        mRefreshLayout.postSetRefreshing(isLoading);
    }


    protected void showError() {
        setLoading(false);
        mRecyclerView.setAdapter(null);
        mErrorView.setVisibility(View.VISIBLE);
    }


}
