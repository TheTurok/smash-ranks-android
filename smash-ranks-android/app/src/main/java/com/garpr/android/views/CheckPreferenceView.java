package com.garpr.android.views;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.garpr.android.R;


public class CheckPreferenceView extends LinearLayout {


    private CheckedTextView mTitle;
    private TextView mSubTitle;




    public CheckPreferenceView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSubTitle = (TextView) findViewById(R.id.view_check_preference_sub_title);
        mTitle = (CheckedTextView) findViewById(R.id.view_check_preference_title);
    }


    private void parseAttributes(final AttributeSet attrs) {
        // TODO
    }


}
