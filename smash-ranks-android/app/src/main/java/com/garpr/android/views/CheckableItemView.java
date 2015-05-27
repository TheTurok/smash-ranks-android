package com.garpr.android.views;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.R;


public class CheckableItemView extends FrameLayout {


    private CheckBox mCheck;
    private LinearLayout mContainer;
    private TextView mText;
    private ViewHolder mViewHolder;




    public static CheckableItemView inflate(final Context context, final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return (CheckableItemView) inflater.inflate(R.layout.view_checkable_item, parent, false);
    }


    public CheckableItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
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
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCheck = (CheckBox) findViewById(R.id.view_checkable_item_check);
        mContainer = (LinearLayout) findViewById(R.id.view_checkable_item_container);
        mText = (TextView) findViewById(R.id.view_checkable_item_text);
    }


    public void setChecked(final boolean checked) {
        mCheck.setChecked(checked);
    }


    public void setOnClickListener(final OnClickListener l) {
        if (l == null) {
            mContainer.setClickable(false);
        } else {
            mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    l.onClick(CheckableItemView.this);
                }
            });
        }
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
