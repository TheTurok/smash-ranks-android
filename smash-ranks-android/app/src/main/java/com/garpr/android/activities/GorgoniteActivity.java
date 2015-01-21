package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.garpr.android.R;


public class GorgoniteActivity extends BaseActivity {


    private static final String TAG = "GorginiteActivity";

    private ImageView mClose;
    private ImageView mImage;




    public static void start(final Activity activity) {
        final Intent intent = new Intent(activity, GorgoniteActivity.class);
        activity.startActivity(intent);
    }


    private void findViews() {
        mClose = (ImageView) findViewById(R.id.activity_gorgonite_close);
        mImage = (ImageView) findViewById(R.id.activity_gorgonite_image);
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
        final View.OnClickListener close = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        };

        mClose.setOnClickListener(close);
        mImage.setOnClickListener(close);
    }


    @Override
    public String toString() {
        return TAG;
    }


}
