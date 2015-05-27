package com.garpr.android.views;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.R;


public class PreferenceView extends LinearLayout {


    private TextView mSubTitle;
    private TextView mTitle;




    public PreferenceView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSubTitle = (TextView) findViewById(R.id.view_preference_sub_title);
        mTitle = (TextView) findViewById(R.id.view_preference_title);
    }


    private void parseAttributes(final AttributeSet attrs) {
        // TODO
    }


}
