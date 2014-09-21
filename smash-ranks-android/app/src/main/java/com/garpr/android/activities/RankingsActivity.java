package com.garpr.android.activities;


import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.garpr.android.R;
import com.garpr.android.misc.Networking;

import org.json.JSONObject;


public class RankingsActivity extends BaseActivity {


    @Override
    protected int getContentView() {
        return R.layout.activity_rankings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloadRankings();
    }

    private void downloadRankings(){
        Networking.Callback callback = new Networking.Callback() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RankingsActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(RankingsActivity.this, response.toString(), Toast.LENGTH_LONG).show();
            }
        };
        Networking.getRankings(callback);

    }
}
