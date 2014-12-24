package com.garpr.android.fragments;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.misc.FlexibleSwipeRefreshLayout;


abstract class BaseListFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener {


    private BaseListAdapter mAdapter;
    private boolean mIsLoading;
    private FlexibleSwipeRefreshLayout mRefreshLayout;
    private LinearLayout mErrorView;
    private RecyclerView mRecyclerView;
    private TextView mErrorLine;




    protected void findViews() {
        final View view = getView();
        mErrorLine = (TextView) view.findViewById(R.id.fragment_base_list_error_line);
        mErrorView = (LinearLayout) view.findViewById(R.id.fragment_base_list_error);
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


    protected void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }


    protected void notifyItemChanged(final int position) {
        if (mAdapter != null) {
            mAdapter.notifyItemChanged(position);
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
        mErrorView.setVisibility(View.GONE);
    }


    protected void prepareViews() {
        mErrorLine.setText(getErrorText());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setRecyclerView(mRecyclerView);
    }


    protected void readArguments(final Bundle arguments) {
        // this method intentionally left blank (children can override)
    }


    protected void setAdapter(final BaseListAdapter adapter) {
        mErrorView.setVisibility(View.GONE);
        mAdapter = adapter;
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




    protected abstract class BaseListAdapter<T extends RecyclerView.ViewHolder> extends
            RecyclerView.Adapter<T> implements View.OnClickListener {


        @Override
        public void onClick(final View v) {
            final int position = mRecyclerView.getChildPosition(v);
            onItemClick(v, position);
        }


    }


}
