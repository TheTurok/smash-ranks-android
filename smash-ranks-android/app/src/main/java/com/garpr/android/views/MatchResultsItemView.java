package com.garpr.android.views;


import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.garpr.android.R;


public class MatchResultsItemView extends FrameLayout implements OnGlobalLayoutListener {


    private boolean mMeasured;
    private boolean mResultsSet;
    private int mLoses;
    private int mWins;
    private TextView mResults;
    private View mLosesBar;
    private View mWinsBar;
    private ViewHolder mViewHolder;




    public static MatchResultsItemView inflate(final Context context, final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return (MatchResultsItemView) inflater.inflate(R.layout.view_match_results_item, parent, false);
    }


    public MatchResultsItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    public int getLoses() {
        return mLoses;
    }


    public TextView getResultsView() {
        return mResults;
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


    private void measureBars() {
        final int totalMatches = mLoses + mWins;

        final int padding = getResources().getDimensionPixelSize(R.dimen.root_padding);
        final int height = mResults.getHeight() - (2 * padding);

        final float losesPixels = (float) mLoses / totalMatches * getWidth();
        final ViewGroup.LayoutParams losesBarParams = mLosesBar.getLayoutParams();
        losesBarParams.height = height;
        losesBarParams.width = (int) losesPixels;
        mLosesBar.setLayoutParams(losesBarParams);

        final float winPixels = (float) mWins / totalMatches * getWidth();
        final ViewGroup.LayoutParams winBarParams = mWinsBar.getLayoutParams();
        winBarParams.height = height;
        winBarParams.width = (int) winPixels;
        mWinsBar.setLayoutParams(winBarParams);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLosesBar = findViewById(R.id.view_match_results_item_loses_bar);
        mWinsBar = findViewById(R.id.view_match_results_item_wins_bar);
        mResults = (TextView) findViewById(R.id.view_match_results_item_results);

        final ViewTreeObserver vto = mResults.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(this);
    }


    @Override
    @SuppressWarnings("deprecation")
    public void onGlobalLayout() {
        final ViewTreeObserver vto = mResults.getViewTreeObserver();

        if (vto.isAlive()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                vto.removeOnGlobalLayoutListener(this);
            } else {
                vto.removeGlobalOnLayoutListener(this);
            }
        }

        mMeasured = true;

        if (mResultsSet) {
            measureBars();
        }
    }


    public void setResults(final int[] results) {
        setResults(results[0], results[1]);
    }


    public void setResults(final int wins, final int loses) {
        mWins = wins;
        mLoses = loses;
        mResultsSet = true;
        mResults.setText(getResources().getString(R.string.x_em_dash_y, mWins, mLoses));

        if (mMeasured) {
            measureBars();
        }
    }




    public final class ViewHolder extends RecyclerView.ViewHolder {


        private ViewHolder() {
            super(MatchResultsItemView.this);
        }


        public MatchResultsItemView getView() {
            return MatchResultsItemView.this;
        }


    }


}
