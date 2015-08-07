package com.garpr.android.views;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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

import java.text.NumberFormat;


public class MatchResultsItemView extends TextView {


    private int mLoses;
    private int mWins;
    private Paint mLosesPaint;
    private Paint mWinsPaint;
    private Rect mLosesRect;
    private Rect mWinsRect;
    private ViewHolder mViewHolder;




    public static MatchResultsItemView inflate(final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return (MatchResultsItemView) inflater.inflate(R.layout.view_match_results_item, parent, false);
    }


    public MatchResultsItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }


    private void calculateRects() {
        // TODO
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


    private void initialize() {
        final Resources res = getResources();

        mLosesRect = new Rect();
        mLosesPaint = new Paint();
        mLosesPaint.setAntiAlias(true);
        mLosesPaint.setColor(res.getColor(R.color.transparent_win_green));
        mLosesPaint.setStyle(Paint.Style.FILL);

        mWinsRect = new Rect();
        mWinsPaint = new Paint();
        mWinsPaint.setAntiAlias(true);
        mWinsPaint.setColor(res.getColor(R.color.transparent_lose_pink));
        mWinsPaint.setStyle(Paint.Style.FILL);
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

        mLosesBar.setVisibility(View.VISIBLE);
        mWinsBar.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (!mLosesRect.isEmpty()) {
            canvas.drawRect(mLosesRect, mLosesPaint);
        }

        if (!mWinsRect.isEmpty()) {
            canvas.drawRect(mWinsRect, mWinsPaint);
        }
    }


    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateRects();
    }


    public void setResults(final int[] results) {
        setResults(results[0], results[1]);
    }


    public void setResults(final int loses, final int wins) {
        mLoses = loses;
        mWins = wins;

        final NumberFormat nf = NumberFormat.getInstance();
        setText(getResources().getString(R.string.x_em_dash_y, nf.format(mWins),
                nf.format(mLoses)));

        calculateRects();
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
