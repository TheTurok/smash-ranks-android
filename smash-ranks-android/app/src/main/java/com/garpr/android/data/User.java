package com.garpr.android.data;


public class User {


    public final IntegerSetting Rank;
    public final PlayerSetting Player;
    public final RegionSetting Region;




    User(final String key) {
        Rank = new IntegerSetting(key + ".RANK", 0);
        Player = new PlayerSetting(key + ".PLAYER");
        Region = new RegionSetting(key + ".REGION");
    }


}
