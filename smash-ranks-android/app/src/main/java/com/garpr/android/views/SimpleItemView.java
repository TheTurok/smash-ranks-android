package com.garpr.android.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.garpr.android.R;


public class SimpleItemView extends FrameLayout implements View.OnClickListener {


    private OnClickListener mClickListener;
    private TextView mText;
    private ViewHolder mViewHolder;




    public static SimpleItemView inflate(final Context context, final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return (SimpleItemView) inflater.inflate(R.layout.view_simple_item, parent, false);
    }


    public SimpleItemView(final Context context) {
        super(context);
    }


    public SimpleItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    public SimpleItemView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SimpleItemView(final Context context, final AttributeSet attrs,
            final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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


    @Override
    public void onClick(final View v) {
        mClickListener.onClick(this);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mText = (TextView) findViewById(R.id.view_simple_item_text);
    }


    public void setOnClickListener(final OnClickListener l) {
        mClickListener = l;
        setOnClickListener(this);
    }


    public void setText(final CharSequence text) {
        mText.setText(text);
    }




    public final class ViewHolder extends RecyclerView.ViewHolder {


        private ViewHolder() {
            super(SimpleItemView.this);
        }


        public SimpleItemView getView() {
            return SimpleItemView.this;
        }


    }


    public interface OnClickListener {


        void onClick(final SimpleItemView v);


    }


}
