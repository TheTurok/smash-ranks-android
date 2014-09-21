package com.garpr.android.activities;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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


public class RankingsActivity extends BaseActivity implements AdapterView.OnItemClickListener{

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
        mListView.setAdapter(new RankingsAdapter());
        mListView.setOnItemClickListener(this);
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Player pee = mPlayers.get(i);
        PlayerActivity.start(this, pee);
    }

    private class RankingsAdapter extends BaseAdapter{

        private final LayoutInflater mInflater;

        private RankingsAdapter() {
            mInflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mPlayers.size();
        }

        @Override
        public Player getItem(final int i) {
            return mPlayers.get(i);
        }

        @Override
        public long getItemId(final int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, final ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(R.layout.model_player, viewGroup, false);
            }

            ViewHolder holder = (ViewHolder) view.getTag();

            if (holder == null) {
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            final Player player = getItem(i);
            holder.mRank.setText(String.valueOf(player.getRank()));
            holder.mName.setText(player.getName());
            holder.mRating.setText(String.format("%.3f", player.getRating()));

            return view;
        }
    }


    private final static class ViewHolder {

        private final TextView mName;
        private final TextView mRank;
        private final TextView mRating;

        private ViewHolder(final View view) {
            mRank = (TextView) view.findViewById(R.id.model_player_rank);
            mName = (TextView) view.findViewById(R.id.model_player_name);
            mRating = (TextView) view.findViewById(R.id.model_player_rating);
        }

    }


}
