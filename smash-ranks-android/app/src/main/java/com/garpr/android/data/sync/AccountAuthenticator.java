package com.garpr.android.data.sync;


import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;


/**
 * This class's code taken entirely from BasicSyncAdapter.zip found here:
 * https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 */
final class AccountAuthenticator extends AbstractAccountAuthenticator {


    private static final String TAG = "AccountAuthenticator";




    AccountAuthenticator(final Context context) {
        super(context);
    }


    @Override
    public Bundle addAccount(final AccountAuthenticatorResponse response, final String accountType,
            final String authTokenType, final String[] requiredFeatures, final Bundle options)
            throws NetworkErrorException {
        return null;
    }


    @Override
    public Bundle confirmCredentials(final AccountAuthenticatorResponse response,
            final Account account, final Bundle options) throws NetworkErrorException {
        return null;
    }


    @Override
    public Bundle editProperties(final AccountAuthenticatorResponse response,
            final String accountType) {
        throw new UnsupportedOperationException("attempted to use editProperties() in " + TAG);
    }


    @Override
    public Bundle getAuthToken(final AccountAuthenticatorResponse response, final Account account,
            final String authTokenType, final Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException("attempted to use getAuthToken() in " + TAG);
    }


    @Override
    public String getAuthTokenLabel(final String authTokenType) {
        throw new UnsupportedOperationException("attempted to use getAuthTokenLabel() in " + TAG);
    }


    @Override
    public Bundle hasFeatures(final AccountAuthenticatorResponse response, final Account account,
            final String[] features) throws NetworkErrorException {
        throw new UnsupportedOperationException("attempted to use hasFeatures() in " + TAG);
    }


    @Override
    public Bundle updateCredentials(final AccountAuthenticatorResponse response,
            final Account account, final String authTokenType, final Bundle options)
            throws NetworkErrorException {
        throw new UnsupportedOperationException("attempted to use updateCredentials in " + TAG);
    }


}
