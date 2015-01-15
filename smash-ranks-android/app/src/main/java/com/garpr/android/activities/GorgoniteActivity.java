package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.garpr.android.R;
import com.garpr.android.misc.Analytics;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.GooglePlayServicesUnavailableException;


public class GorgoniteActivity extends Activity {


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
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gorgonite);
        findViews();
        prepareViews();
        Toast.makeText(this, R.string.no_gorgonite_johns, Toast.LENGTH_LONG).show();

        try {
            Analytics.report(TAG).sendScreenView();
        } catch (final GooglePlayServicesUnavailableException e) {
            Console.w(TAG, "Unable to report screen view for " + TAG + " to analytics", e);
        }
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
