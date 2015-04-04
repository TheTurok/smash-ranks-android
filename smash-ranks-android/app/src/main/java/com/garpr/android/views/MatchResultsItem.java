package com.garpr.android.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.garpr.android.R;


public class MatchResultsItem extends TextView {


    private ViewHolder mViewHolder;




    public MatchResultsItem(final Context context) {
        super(context);
    }


    public MatchResultsItem(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    public MatchResultsItem(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MatchResultsItem(final Context context, final AttributeSet attrs,
            final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public ViewHolder getViewHolder() {
        if (mViewHolder == null) {
            mViewHolder = new ViewHolder();
        }

        return mViewHolder;
    }




    public final class ViewHolder extends RecyclerView.ViewHolder {


        private ViewHolder() {
            super(MatchResultsItem.this);
        }


        public MatchResultsItem getView() {
            return MatchResultsItem.this;
        }


    }


}
