package com.garpr.android.data.sync;


import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public final class AccountService extends Service {


    private static final String ACCOUNT_NAME = "";
    private static final String ACCOUNT_TYPE = "";
    private static final String TAG = AccountService.class.getSimpleName();

    private AccountAuthenticator mAuthenticator;




    public static Account getAccount() {
        return new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    }


    @Override
    public IBinder onBind(final Intent intent) {
        return mAuthenticator.getIBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator = new AccountAuthenticator(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
