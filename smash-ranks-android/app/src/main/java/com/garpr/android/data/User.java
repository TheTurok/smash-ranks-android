package com.garpr.android.data;


public final class User {


    private static final String CNAME = "com.garpr.android.data.User";

    public static final IntegerSetting Rank;
    public static final PlayerSetting Player;
    public static final RegionSetting Region;




    static {
        Rank = new IntegerSetting(CNAME + ".Rank", 0);
        Player = new PlayerSetting(CNAME + ".Player");
        Region = new RegionSetting(CNAME + ".Region");
    }


}
