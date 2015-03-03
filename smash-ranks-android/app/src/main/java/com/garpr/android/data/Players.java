package com.garpr.android.data;


import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.VolleyError;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;
import com.garpr.android.misc.HeartbeatWithUi;
import com.garpr.android.models.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.SharedPreferences.Editor;


public final class Players {


    private static final SimpleDateFormat DATE_PARSER;
    private static final String CNAME = "com.garpr.android.data.Players";
    private static final String KEY_ROSTER_UPDATE = "KEY_ROSTER_UPDATE";
    private static final String TAG = "Players";




    static {
        DATE_PARSER = new SimpleDateFormat(Constants.RANKINGS_DATE_FORMAT);
    }


    public static void checkForRosterUpdate(final RosterUpdateCallback callback) {
        final String url = Network.makeUrl(Constants.RANKINGS);
        Network.sendRequest(url, callback);
    }


    public static void clear() {
        final String tableName = getTableName();
        Database.truncateTable(tableName);
    }


    private static ContentValues createContentValues(final Player player) {
        final JSONObject playerJSON = player.toJSON();
        final String playerString = playerJSON.toString();

        final ContentValues values = new ContentValues();
        values.put(Constants.ID, player.getId());
        values.put(Constants.JSON, playerString);

        return values;
    }


    public static void getAll(final PlayersCallback callback) {
        final AsyncReadPlayersDatabase task = new AsyncReadPlayersDatabase(callback);
        task.start();
    }


    public static long getMostRecentRosterUpdate() {
        final SharedPreferences sPreferences = Settings.get(CNAME);
        return sPreferences.getLong(KEY_ROSTER_UPDATE, 0L);
    }


    public static void getRankings(final PlayersCallback callback) {
        final PlayersCallback callbackWrapper0 = new PlayersCallback(callback.getHeartbeat()) {
            @Override
            public void response(final Exception e) {
                callback.response(e);
            }


            @Override
            public void response(final ArrayList<Player> list) {
                if (playersHaveRankings(list)) {
                    Console.d(TAG, "Found rankings in the list of players");
                    stripListOfRankinglessPlayers(list);
                    callback.response(list);
                } else {
                    Console.d(TAG, "Rankings were not found in the list of players");

                    final RankingsCallback callbackWrapper1 = new RankingsCallback(callback.getHeartbeat()) {
                        @Override
                        public void response(final Exception e) {
                            callback.response(e);
                        }


                        @Override
                        public void response(final ArrayList<Player> list) {
                            callback.response(list);
                        }
                    };

                    getRankingsFromNetwork(callbackWrapper1);
                }
            }
        };

        getAll(callbackWrapper0);
    }


    private static void getPlayersFromNetwork(final PlayersCallback callback) {
        if (callback.isAlive()) {
            Console.d(TAG, "Grabbing players from network");
            final String url = Network.makeUrl(Constants.PLAYERS);
            Network.sendRequest(url, callback);
        } else {
            Console.d(TAG, "Canceled grabbing players from network");
        }
    }


    private static void getRankingsFromNetwork(final RankingsCallback callback) {
        if (callback.isAlive()) {
            Console.d(TAG, "Grabbing rankings from network");
            final String url = Network.makeUrl(Constants.RANKINGS);
            Network.sendRequest(url, callback);
        } else {
            Console.d(TAG, "Canceled grabbing rankings from network");
        }
    }


    static String getTableName() {
        return (TAG + '_' + Settings.getRegion().getId()).toLowerCase();
    }


    private static ArrayList<Player> parseJSON(final JSONObject json) throws JSONException {
        final JSONArray playersJSON;

        if (json.has(Constants.PLAYERS)) {
            playersJSON = json.getJSONArray(Constants.PLAYERS);
        } else if (json.has(Constants.RANKING)) {
            playersJSON = json.getJSONArray(Constants.RANKING);
        } else {
            throw new JSONException("Neither " + Constants.PLAYERS + " nor " + Constants.RANKING
                    + " exists in the JSON");
        }

        final int playersLength = playersJSON.length();
        final ArrayList<Player> players = new ArrayList<>(playersLength);

        for (int i = 0; i < playersLength; ++i) {
            final JSONObject playerJSON = playersJSON.getJSONObject(i);
            final Player player = new Player(playerJSON);
            players.add(player);
        }

        players.trimToSize();
        return players;
    }


    private static long parseRosterUpdate(final JSONObject json) throws JSONException {
        final String dateString = json.getString(Constants.TIME);

        try {
            final Date date = DATE_PARSER.parse(dateString);
            final long time = date.getTime();

            final Editor editor = Settings.edit(CNAME);
            editor.putLong(KEY_ROSTER_UPDATE, time);
            editor.apply();
        } catch (final ParseException e) {
            throw new JSONException("Couldn't parse the date: \"" + dateString + "\"");
        }

        return getMostRecentRosterUpdate();
    }


    private static boolean playersHaveRankings(final ArrayList<Player> list) {
        boolean playersHaveRankings = false;

        for (int i = 0; i < list.size() && !playersHaveRankings; ++i) {
            final Player player = list.get(i);

            if (player.hasCompetitionValues()) {
                playersHaveRankings = true;
            }
        }

        return playersHaveRankings;
    }


    public static void save(final Player player) {
        final ContentValues values = createContentValues(player);
        final String whereClause = Constants.ID + " = ?";
        final String[] whereArgs = { player.getId() };

        final SQLiteDatabase database = Database.start();
        database.update(getTableName(), values, whereClause, whereArgs);
        Database.stop();
    }


    private static void savePlayers(final ArrayList<Player> players) {
        final AsyncSavePlayersDatabase task = new AsyncSavePlayersDatabase(players);
        task.start();
    }


    private static void saveRankings(final ArrayList<Player> players) {
        final AsyncSaveRankingsDatabase task = new AsyncSaveRankingsDatabase(players);
        task.start();
    }


    private static void stripListOfRankinglessPlayers(final ArrayList<Player> players) {
        for (int i = 0; i < players.size(); ) {
            final Player player = players.get(i);

            if (player.hasCompetitionValues()) {
                ++i;
            } else {
                players.remove(i);
            }
        }

        players.trimToSize();
    }




    private static final class AsyncReadPlayersDatabase extends AsyncReadDatabase<Player> {


        private static final String TAG = "AsyncReadPlayersDatabase";


        private AsyncReadPlayersDatabase(final PlayersCallback callback) {
            super(callback, getTableName());
        }


        @Override
        Player createItem(final JSONObject json) throws JSONException {
            return new Player(json);
        }


        @Override
        String getAsyncRunnableName() {
            return TAG;
        }


        @Override
        void getFromNetwork(final Callback<Player> callback) {
            getPlayersFromNetwork((PlayersCallback) callback);
        }


    }


    private static final class AsyncSavePlayersDatabase extends AsyncSaveDatabase<Player> {


        private static final String TAG = "AsyncSavePlayersDatabase";


        private AsyncSavePlayersDatabase(final ArrayList<Player> players) {
            super(players, getTableName());
        }


        @Override
        void clear() {
            Players.clear();
        }


        @Override
        String getAsyncRunnableName() {
            return TAG;
        }


        @Override
        void transact(final SQLiteDatabase database, final String tableName, final Player item) {
            final ContentValues values = createContentValues(item);
            database.insert(tableName, null, values);
        }


    }


    private static class AsyncSaveRankingsDatabase extends AsyncSaveDatabase<Player> {


        private static final String TAG = "AsyncSaveRankingsDatabase";

        private final String mWhereClause;


        private AsyncSaveRankingsDatabase(final ArrayList<Player> players) {
            super(players, getTableName());
            mWhereClause = Constants.ID + " = ?";
        }


        @Override
        String getAsyncRunnableName() {
            return TAG;
        }


        @Override
        void transact(final SQLiteDatabase database, final String tableName, final Player item) {
            final ContentValues values = createContentValues(item);
            final String[] whereArgs = { item.getId() };
            database.update(tableName, values, mWhereClause, whereArgs);
        }


    }


    public static abstract class PlayersCallback extends CallbackWithUi<Player> {


        private static final String TAG = "PlayersCallback";


        public PlayersCallback(final HeartbeatWithUi heartbeat) {
            super(heartbeat);
        }


        @Override
        final String getCallbackName() {
            return TAG;
        }


        @Override
        final void onItemResponse(final Player item) {
            final ArrayList<Player> players = new ArrayList<>(1);
            players.add(item);
            onListResponse(players);
        }


        @Override
        final void onJSONResponse(final JSONObject json) {
            try {
                final ArrayList<Player> players = parseJSON(json);
                Console.d(TAG, "Read in " + players.size() + " Player objects from players JSON response");

                if (players.isEmpty()) {
                    responseOnUi(new JSONException("No players grabbed from players JSON response"));
                } else {
                    savePlayers(players);
                    responseOnUi(players);
                }
            } catch (final JSONException e) {
                responseOnUi(e);
            }
        }


        @Override
        public final void response(final Player item) {
            throw new UnsupportedOperationException();
        }


    }




    private static abstract class RankingsCallback extends CallbackWithUi<Player> {


        private static final String TAG = "RankingsCallback";


        private RankingsCallback(final HeartbeatWithUi heartbeat) {
            super(heartbeat);
        }


        @Override
        final String getCallbackName() {
            return TAG;
        }


        @Override
        final void onItemResponse(final Player item) {
            final ArrayList<Player> items = new ArrayList<>(1);
            items.add(item);
            onListResponse(items);
        }


        @Override
        final void onJSONResponse(final JSONObject json) {
            try {
                final ArrayList<Player> rankings = parseJSON(json);
                Console.d(TAG, "Read in " + rankings.size() + " Player objects from rankings JSON response");

                if (rankings.isEmpty()) {
                    responseOnUi(new JSONException("No players grabbed from rankings JSON response"));
                } else {
                    parseRosterUpdate(json);
                    saveRankings(rankings);
                    responseOnUi(rankings);
                }
            } catch (final JSONException e) {
                responseOnUi(e);
            }
        }


        @Override
        public final void response(final Player item) {
            throw new UnsupportedOperationException();
        }


    }


    public static abstract class RosterUpdateCallback extends Callback<Player> {


        private static final String TAG = "RosterUpdateCallback";


        public RosterUpdateCallback(final Heartbeat heartbeat) {
            super(heartbeat);
        }


        @Override
        final String getCallbackName() {
            return TAG;
        }


        private void getPlayersFromNetwork(final ArrayList<Player> rankings) {
            final WeakReference<Heartbeat> heartbeat = new WeakReference<>(getHeartbeat());

            final HeartbeatWithUi heartbeatWithUi = new HeartbeatWithUi() {
                @Override
                public boolean isAlive() {
                    final Heartbeat hb = heartbeat.get();
                    return hb != null && hb.isAlive();
                }


                @Override
                public void runOnUi(final Runnable action) {
                    action.run();
                }
            };

            final PlayersCallback callback = new PlayersCallback(heartbeatWithUi) {
                @Override
                public void response(final Exception e) {
                    RosterUpdateCallback.this.response(e);
                }


                @Override
                public void response(final ArrayList<Player> list) {
                    for (final Player player : list) {
                        final int index = rankings.indexOf(player);

                        if (index != -1) {
                            final Player rank = list.get(index);
                            list.set(index, rank);
                        }
                    }

                    Tournaments.clear();
                    savePlayers(list);

                    if (isAlive()) {
                        newRosterAvailable();
                    } else {
                        Console.w(TAG, "A new roster is available but the listener is dead");
                    }
                }
            };

            Players.getPlayersFromNetwork(callback);
        }


        @Override
        public final void onErrorResponse(final VolleyError error) {
            if (isAlive()) {
                response(error);
            } else {
                Console.e(TAG, "Exception when downloading roster", error);
            }
        }


        @Override
        final void onItemResponse(final Player item) {
            throw new UnsupportedOperationException();
        }


        @Override
        final void onJSONResponse(final JSONObject json) {
            final long lastUpdate = getMostRecentRosterUpdate();

            try {
                final long currentUpdate = parseRosterUpdate(json);

                if (currentUpdate > lastUpdate) {
                    Console.d(TAG, "A new roster is available: " + currentUpdate + " vs " + lastUpdate);
                    final ArrayList<Player> rankings = parseJSON(json);

                    if (rankings.isEmpty()) {
                        response(new Exception("empty roster"));
                    } else {
                        getPlayersFromNetwork(rankings);
                    }
                } else if (isAlive()) {
                    noNewRoster();
                } else {
                    Console.w(TAG, "There is no new roster and the listener is dead");
                }
            } catch (final JSONException e) {
                response(e);
            }
        }


        @Override
        final void onListResponse(final ArrayList<Player> list) {
            throw new UnsupportedOperationException();
        }


        @Override
        public final void response(final Player item) {
            throw new UnsupportedOperationException();
        }


        @Override
        public final void response(final ArrayList<Player> list) {
            throw new UnsupportedOperationException();
        }


        public abstract void newRosterAvailable();


        public abstract void noNewRoster();


    }


}
