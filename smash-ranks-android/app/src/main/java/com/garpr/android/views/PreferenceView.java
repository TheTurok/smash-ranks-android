package com.garpr.android.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.R;


public class PreferenceView extends LinearLayout {


    private TextView mSubTitle;
    private TextView mTitle;




    public PreferenceView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    public void disable() {
        setEnabled(false);
        setAlpha(0.6f);
    }


    public void enable() {
        setAlpha(1f);
        setEnabled(true);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSubTitle = (TextView) findViewById(R.id.view_preference_sub_title);
        mTitle = (TextView) findViewById(R.id.view_preference_title);
    }


    public void setOnClickListener(final OnClickListener l) {
        if (l == null) {
            setClickable(false);
        } else {
            setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    l.onClick(PreferenceView.this);
                }
            });
        }
    }


    public void setSubTitleText(final int resId) {
        mSubTitle.setText(resId);
    }


    public void setTitleText(final int resId) {
        mTitle.setText(resId);
    }




    public interface OnClickListener {


        void onClick(final PreferenceView v);


    }


}
