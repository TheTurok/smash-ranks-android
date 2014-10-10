package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.garpr.android.R;
import com.garpr.android.models.Match;
import com.garpr.android.models.Player;

import java.util.ArrayList;


public class PlayerActivity extends BaseActivity {


    private static final String CNAME = PlayerActivity.class.getCanonicalName();
    private static final String EXTRA_PLAYER = CNAME + ".EXTRA_PLAYER";

    private ArrayList<Match> mMatches;
    private ListView mListView;
    private Player mPlayer;
    private TextView mRank;




    public static void start(final Activity activity, final Player player) {
        final Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra(EXTRA_PLAYER, player);
        activity.startActivity(intent);
    }


    protected void downloadMatches() {
        // TODO
    }


    @Override
    protected void findViews() {
        super.findViews();
        mListView = (ListView) findViewById(R.id.activity_player_list);
        mRank = (TextView) findViewById(R.id.activity_player_rank);
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_player;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntent();
        findViews();
        prepareViews();

        if (mPlayer.hasMatches()) {
            mMatches = mPlayer.getMatches();
            showList();
        } else {
            downloadMatches();
        }
    }


    private void prepareViews() {
        setTitle(mPlayer.getName());
        mRank.setText(String.valueOf(mPlayer.getRank()));
    }


    private void readIntent() {
        final Intent intent = getIntent();
        mPlayer = intent.getParcelableExtra(EXTRA_PLAYER);
    }


    protected void showList() {
        // TODO
    }


}
