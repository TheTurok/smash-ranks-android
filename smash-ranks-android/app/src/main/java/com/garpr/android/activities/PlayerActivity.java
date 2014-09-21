package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.models.Player;

public class PlayerActivity extends BaseActivity {


    private static final String CNAME = PlayerActivity.class.getCanonicalName();
    private static final String EXTRA_PLAYER = CNAME + ".EXTRA_PLAYER";

    private ListView mList;
    private Player mPlayer;
    private TextView mName;
    private TextView mRank;




    public static void start(final Activity activity, final Player player) {
        final Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra(EXTRA_PLAYER, player);
        activity.startActivity(intent);
    }


    private void findViews() {
        mList = (ListView) findViewById(R.id.activity_player_list);
        mName = (TextView) findViewById(R.id.activity_player_name);
        mRank = (TextView) findViewById(R.id.activity_player_rank);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_player ;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntent();
        findViews();
        prepareViews();
    }


    private void prepareViews() {
        mName.setText(mPlayer.getName());
        mRank.setText(String.valueOf(mPlayer.getRank()));
        mRank.setText(R.string.hello_world);
    }


    private void readIntent() {
        final Intent intent = getIntent();
        mPlayer = intent.getParcelableExtra(EXTRA_PLAYER);
    }


}
