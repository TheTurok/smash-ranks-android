package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.garpr.android.App;


public final class Settings {


    public static Editor edit(final String name) {
        return get(name).edit();
    }


    public static SharedPreferences get(final String name) {
        return App.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }


}
