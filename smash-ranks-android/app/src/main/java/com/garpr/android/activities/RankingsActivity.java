package com.garpr.android.activities;


import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.garpr.android.R;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Networking;
import com.garpr.android.models.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RankingsActivity extends BaseActivity {

    private ArrayList<Player> mPlayers;
    private ListView mListView;
    private ProgressBar mProgress;

    @Override
    protected int getContentView() {
        return R.layout.activity_rankings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        downloadRankings();
    }

    private void findViews(){
        mListView = (ListView) findViewById(R.id.activity_rankings_list);
        mProgress = (ProgressBar) findViewById(R.id.progress);
    }

    private void showList(){
        mProgress.setVisibility(View.GONE);
        

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

                try {
                    ArrayList<Player> playersList = new ArrayList<Player>();
                    JSONArray ranking = response.getJSONArray(Constants.RANKING);
                    for(int i = 0; i < ranking.length() ; ++i ){
                        JSONObject playerJSON = ranking.getJSONObject(i);

                        try {
                            Player player = new Player(playerJSON);
                            playersList.add(player);
                        } catch (JSONException e) {
                            //nothing
                        }
                    }
                    playersList.trimToSize();
                    mPlayers = playersList;
                    showList();
                } catch (JSONException e) {

                }

            }
        };
        Networking.getRankings(callback);

    }
}
