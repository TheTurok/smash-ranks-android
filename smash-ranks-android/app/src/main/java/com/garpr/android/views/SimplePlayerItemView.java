package com.garpr.android.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;


public class SimplePlayerItemView extends FrameLayout {


    private ViewHolder mViewHolder;




    public SimplePlayerItemView(final Context context) {
        super(context);
    }


    public SimplePlayerItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    public SimplePlayerItemView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SimplePlayerItemView(final Context context, final AttributeSet attrs,
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


        public ViewHolder() {
            super(SimplePlayerItemView.this);
        }


        public SimplePlayerItemView getView() {
            return SimplePlayerItemView.this;
        }


    }


}
