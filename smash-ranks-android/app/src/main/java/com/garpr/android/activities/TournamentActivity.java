package com.garpr.android.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.garpr.android.R;
import com.garpr.android.models.Tournament;


public class TournamentActivity extends BaseToolbarActivity {


    private static final String CNAME = "com.garpr.android.activities.TournamentActivity";
    private static final String EXTRA_TOURNAMENT = CNAME + ".EXTRA_TOURNAMENT";
    private static final String TAG = "TournamentActivity";

    private Tournament mTournament;




    public static void start(final Activity activity, final Tournament tournament) {
        final Intent intent = new Intent(activity, TournamentActivity.class);
        intent.putExtra(EXTRA_TOURNAMENT, tournament);
        activity.startActivity(intent);
    }


    @Override
    protected String getActivityName() {
        return TAG;
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_tournament;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntentData();
        setTitle(mTournament.getName());
    }


    private void readIntentData() {
        final Intent intent = getIntent();
        mTournament = intent.getParcelableExtra(EXTRA_TOURNAMENT);
    }


}
