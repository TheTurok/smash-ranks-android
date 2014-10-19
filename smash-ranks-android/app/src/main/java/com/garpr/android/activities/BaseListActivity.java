package com.garpr.android.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.misc.FadeAnimator;
import com.garpr.android.misc.FlexibleSwipeRefreshLayout;


abstract class BaseListActivity extends BaseActivity implements
        AbsListView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {


    private BaseListAdapter mAdapter;
    private boolean mIsLoading;
    private FadeAnimator mErrorAnimator;
    private FadeAnimator mListAnimator;
    private FlexibleSwipeRefreshLayout mRefreshLayout;
    private ListView mListView;
    private TextView mErrorView;




    private FadeAnimator animate(FadeAnimator animator, final View view, final boolean fadeIn) {
        if (animator != null) {
            animator.cancelIfRunning();
        }

        final int visibility = view.getVisibility();

        if ((fadeIn && visibility == View.VISIBLE) || (!fadeIn && visibility == View.GONE)) {
            return animator;
        }

        if (fadeIn) {
            animator = FadeAnimator.fadeIn(view);
        } else {
            animator = FadeAnimator.fadeOut(view);
        }

        animator.start();
        return animator;
    }


    private void animateError(final boolean fadeIn) {
        mErrorAnimator = animate(mErrorAnimator, mErrorView, fadeIn);
    }


    private void animateList(final boolean fadeIn) {
        mListAnimator = animate(mListAnimator, mListView, fadeIn);
    }


    private void findViews() {
        mErrorView = (TextView) findViewById(R.id.activity_base_list_error);
        mListView = (ListView) findViewById(R.id.activity_base_list_list);
        mRefreshLayout = (FlexibleSwipeRefreshLayout) findViewById(R.id.activity_base_list_refresh);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_base_list;
    }


    protected String getErrorText() {
        return getString(R.string.error);
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


    protected void onItemClick(final Object item) {

    }


    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position,
            final long id) {
        final Object item = parent.getItemAtPosition(position);
        onItemClick(item);
    }


    @Override
    public void onRefresh() {
        animateError(false);
    }


    private void prepareViews() {
        mErrorView.setText(getErrorText());
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setScrollableView(mListView);
    }


    protected void readIntentData(final Intent intent) {
        // this method intentionally left blank, override to perform custom actions
    }


    protected void showError() {
        setLoading(false);
        animateList(false);
        animateError(true);
    }


    protected void setAdapter(final BaseListAdapter adapter) {
        animateError(false);
        mAdapter = adapter;
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        animateList(true);
        setLoading(false);
    }


    protected void setLoading(final boolean loading) {
        mRefreshLayout.setRefreshing(loading);
        mIsLoading = loading;
    }




    protected abstract class BaseListAdapter extends BaseAdapter {


        protected final LayoutInflater mInflater;


        protected BaseListAdapter() {
            mInflater = getLayoutInflater();
        }


        @Override
        public long getItemId(final int position) {
            return position;
        }


    }


}
