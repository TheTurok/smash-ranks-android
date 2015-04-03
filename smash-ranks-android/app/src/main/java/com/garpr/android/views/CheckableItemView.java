package com.garpr.android.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;

import com.garpr.android.R;


public class CheckableItemView extends FrameLayout {


    private CheckedTextView mText;
    private ViewHolder mViewHolder;




    public CheckableItemView(final Context context) {
        super(context);
    }


    public CheckableItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    public CheckableItemView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckableItemView(final Context context, final AttributeSet attrs,
            final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public CheckedTextView getTextView() {
        return mText;
    }


    public ViewHolder getViewHolder() {
        if (mViewHolder == null) {
            mViewHolder = new ViewHolder();
        }

        return mViewHolder;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mText = (CheckedTextView) findViewById(R.id.model_checkable_text);
    }




    public final class ViewHolder extends RecyclerView.ViewHolder {


        private ViewHolder() {
            super(CheckableItemView.this);
        }


        public CheckableItemView getView() {
            return CheckableItemView.this;
        }


    }


}
