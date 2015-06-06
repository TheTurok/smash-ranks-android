package com.garpr.android.settings;


public final class Sync {


    private static final String CNAME = "com.garpr.android.settings.Sync";

    public static final BooleanSetting ChargingNecessary;
    public static final BooleanSetting Enabled;
    public static final BooleanSetting WifiNecessary;




    static {
        ChargingNecessary = new BooleanSetting(CNAME + ".CHARGING_NECESSARY", false);
        Enabled = new BooleanSetting(CNAME + ".ENABLED", true);
        WifiNecessary = new BooleanSetting(CNAME + ".WIFI_NECESSARY", true);
    }


}
