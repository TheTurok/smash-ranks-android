package com.garpr.android.data.sync;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.misc.Console;


/**
 * This class's code taken entirely from BasicSyncAdapter.zip found here:
 * https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 */
public final class Sync {


    private static final String TAG = Sync.class.getSimpleName();




    public static void setup() {
        final Context context = App.getContext();
        final String accountName = context.getString(R.string.gar_pr);
        final String accountType = context.getPackageName();
        final Account account = new Account(accountName, accountType);

        final AccountManager am = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        final String packageName = context.getPackageName();

        if (am.addAccountExplicitly(account, null, null)) {
            Console.d(TAG, "Account manager is explicitly adding the account");
            ContentResolver.setIsSyncable(account, packageName, 1);
            ContentResolver.setSyncAutomatically(account, packageName, true);
        } else {
            Console.d(TAG, "Account manager did not need to explicitly add the account");
        }
    }


}
