package com.garpr.android.misc;


import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.AbsListView;

import com.garpr.android.R;


/**
 * An extension of the support library's {@link SwipeRefreshLayout} view that allows for its
 * single child view to be something other than a typical Android {@link AbsListView}.
 * <p/>
 * This is talked about a bit more on Stack Overflow: http://stackoverflow.com/a/24266857/823952
 * <p/>
 * Without this class, SwipeRefreshLayout has an annoying issue with not allowing its child
 * ListView to scroll up. So you'll be able to scroll down, but then once you try to go up, you
 * wouldn't be able to!
 * <p/>
 * Maybe at a future date Google will fix this issue with a new version of the support library...
 */
public final class FlexibleSwipeRefreshLayout extends SwipeRefreshLayout {


    private RecyclerView mRecyclerView;




    public FlexibleSwipeRefreshLayout(final Context context) {
        super(context);
    }


    public FlexibleSwipeRefreshLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setColorSchemeResources(R.color.indigo_dark, R.color.indigo, R.color.indigo_bright);
    }


    @Override
    public boolean canChildScrollUp() {
        final boolean canChildScrollUp;

        if (mRecyclerView == null) {
            canChildScrollUp = super.canChildScrollUp();
        } else {
            // -1 means to check scrolling up
            canChildScrollUp = ViewCompat.canScrollVertically(mRecyclerView, -1);
        }

        return canChildScrollUp;
    }


    /**
     * Normally we'd just use {@link #setRefreshing(boolean)} instead, but there's a bug with that:
     * https://code.google.com/p/android/issues/detail?id=77712 hopefully in a future version of
     * the appcompat library, we can get rid of this work around.
     */
    public void postSetRefreshing(final boolean isLoading) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                setRefreshing(isLoading);
            }
        };

        post(runnable);
    }


    public void setRecyclerView(final RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }


}
