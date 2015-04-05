package com.garpr.android.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.garpr.android.R;


public class MatchResultsItem extends TextView {


    private int mLoses;
    private int mWins;
    private ViewHolder mViewHolder;




    public static MatchResultsItem inflate(final Context context, final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return (MatchResultsItem) inflater.inflate(R.layout.view_match_results_item, parent, false);
    }


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


    public int getLoses() {
        return mLoses;
    }


    public int getWins() {
        return mWins;
    }


    public ViewHolder getViewHolder() {
        if (mViewHolder == null) {
            mViewHolder = new ViewHolder();
        }

        return mViewHolder;
    }


    public void setResults(final int wins, final int loses) {
        mWins = wins;
        mLoses = loses;
        setText(getResources().getString(R.string.x_em_dash_y, mWins, mLoses));
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
