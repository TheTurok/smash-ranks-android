package com.garpr.android.misc;


import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.widget.AbsListView;


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
public class FlexibleSwipeRefreshLayout extends SwipeRefreshLayout {


    private AbsListView mAbsListView;




    public FlexibleSwipeRefreshLayout(final Context context) {
        super(context);
    }


    public FlexibleSwipeRefreshLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    private boolean canAbsListViewScrollUp() {
        // Taken directly from the source for SwipeRefreshLayout:
        // https://github.com/futuresimple/android-support-v4/blob/master/src/java/android/support/v4/widget/SwipeRefreshLayout.java#L348
        return mAbsListView.getChildCount() > 0 && (mAbsListView.getFirstVisiblePosition() > 0
                || mAbsListView.getChildAt(0).getTop() < mAbsListView.getPaddingTop());
    }


    @Override
    public boolean canChildScrollUp() {
        final boolean canChildScrollUp;

        if (mAbsListView == null) {
            canChildScrollUp = super.canChildScrollUp();
        } else {
            canChildScrollUp = canAbsListViewScrollUp();
        }

        return canChildScrollUp;
    }


    public void setScrollableView(final AbsListView scrollableView) {
        mAbsListView = scrollableView;
    }


}
