package com.garpr.android.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.R;


public class CheckableItemView extends FrameLayout implements View.OnClickListener {


    private CheckBox mCheck;
    private LinearLayout mContainer;
    private OnClickListener mClickListener;
    private TextView mText;
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


    public CheckBox getCheckView() {
        return mCheck;
    }


    public LinearLayout getContainerView() {
        return mContainer;
    }


    public TextView getTextView() {
        return mText;
    }


    public ViewHolder getViewHolder() {
        if (mViewHolder == null) {
            mViewHolder = new ViewHolder();
        }

        return mViewHolder;
    }


    public boolean isChecked() {
        return mCheck.isChecked();
    }


    @Override
    public void onClick(final View v) {
        if (mClickListener != null) {
            mClickListener.onClick(this);
        }
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCheck = (CheckBox) findViewById(R.id.view_checkable_item_check);
        mContainer = (LinearLayout) findViewById(R.id.view_checkable_item_container);
        mText = (TextView) findViewById(R.id.view_checkable_item_text);

        mContainer.setOnClickListener(this);
    }


    public void setChecked(final boolean checked) {
        mCheck.setChecked(checked);
    }


    public void setOnClickListener(final OnClickListener l) {
        mClickListener = l;
    }


    public void setText(final CharSequence text) {
        mText.setText(text);
    }




    public final class ViewHolder extends RecyclerView.ViewHolder {


        private ViewHolder() {
            super(CheckableItemView.this);
        }


        public CheckableItemView getView() {
            return CheckableItemView.this;
        }


    }


    public interface OnClickListener {


        void onClick(final CheckableItemView v);


    }


}
