package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.garpr.android.R;


public class GorgoniteActivity extends BaseActivity {


    private static final String TAG = "GorgoniteActivity";

    private ImageButton mClose;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, GorgoniteActivity.class);
        activity.startActivity(intent);
    }


    private void findViews() {
        mClose = (ImageButton) findViewById(R.id.activity_gorgonite_close);
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_gorgonite;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        prepareViews();
        Toast.makeText(this, R.string.no_gorgonite_johns, Toast.LENGTH_LONG).show();
    }


    private void prepareViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            applyStatusBarHeightAsTopMargin(mClose, true);
        }

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
    }


}
