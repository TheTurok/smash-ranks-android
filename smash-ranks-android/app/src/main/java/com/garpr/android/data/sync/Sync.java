package com.garpr.android.data.sync;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Settings;


/**
 * This class's code taken entirely from BasicSyncAdapter.zip found here:
 * https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 */
public final class Sync {


    private static final String CNAME = Sync.class.getCanonicalName();
    private static final String KEY_SETUP_COMPLETE = "KEY_SETUP_COMPLETE";
    private static final String TAG = Sync.class.getSimpleName();




    public static void setup() {
        boolean newAccount = false;
        final SharedPreferences sPreferences = Settings.get(CNAME);
        final boolean isSetupComplete = sPreferences.getBoolean(KEY_SETUP_COMPLETE, false);

        final Context context = App.getContext();
        final String accountName = context.getString(R.string.gar_pr);
        final String accountType = context.getPackageName();
        final Account account = new Account(accountName, accountType);

        final AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        final String packageName = context.getPackageName();

        if (accountManager.addAccountExplicitly(account, null, null)) {
            Log.d(TAG, "Account manager is explicitly adding the account");
            ContentResolver.setIsSyncable(account, packageName, 1);
            ContentResolver.setSyncAutomatically(account, packageName, true);
            newAccount = true;
        } else {
            Log.d(TAG, "Account manager did not need to explicitly add the account");
        }

        if (newAccount || !isSetupComplete) {
            final Editor editor = sPreferences.edit();
            editor.putBoolean(KEY_SETUP_COMPLETE, true);
            editor.apply();

            Log.d(TAG, "New account created, refreshing now...");
            ContentResolver.requestSync(account, packageName, new Bundle());
        } else {
            Log.d(TAG, "Account refresh unnecessary");
        }
    }


}
