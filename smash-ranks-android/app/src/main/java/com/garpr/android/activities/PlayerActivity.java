package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.garpr.android.R;
import com.garpr.android.models.Player;

public class PlayerActivity extends BaseActivity {


    private static final String CNAME = PlayerActivity.class.getCanonicalName();
    private static final String EXTRA_PLAYER = CNAME + ".EXTRA_PLAYER";

    private Player mPlayer;




    public static void start(final Activity activity, final Player player) {
        final Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra(EXTRA_PLAYER, player);
        activity.startActivity(intent);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_player ;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntent();
    }


    private void readIntent() {
        final Intent intent = getIntent();
        mPlayer = intent.getParcelableExtra(EXTRA_PLAYER);
    }


}
