package com.garpr.android.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.garpr.android.R;


public class NavigationHeaderView extends RelativeLayout {


    private TextView mPlayer;
    private TextView mRegion;




    public NavigationHeaderView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }


    public NavigationHeaderView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NavigationHeaderView(final Context context, final AttributeSet attrs,
            final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPlayer = (TextView) findViewById(R.id.navigation_header_view_player);
        mRegion = (TextView) findViewById(R.id.navigation_header_view_region);
    }


}
