package com.garpr.android.data;


public final class User {


    private static final String CNAME = "com.garpr.android.data.User";

    public static final IntegerSetting Rank;
    public static final PlayerSetting Player;
    public static final RegionSetting Region;




    static {
        Rank = new IntegerSetting(CNAME + ".RANK", 0);
        Player = new PlayerSetting(CNAME + ".PLAYER");
        Region = new RegionSetting(CNAME + ".REGION");
    }


    public static boolean areWeInTheUsersRegion() {
        return Region.get().equals(Settings.Region.get());
    }


    public static boolean hasPlayer() {
        return Player.get() != null;
    }


}
