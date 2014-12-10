package com.garpr.android.data.sync;


import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;


public final class SyncAdapter extends AbstractThreadedSyncAdapter {


    SyncAdapter(final Context context, final boolean autoInitialize,
            final boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }


    @Override
    public void onPerformSync(final Account account, final Bundle extras, final String authority,
            final ContentProviderClient provider, final SyncResult syncResult) {
        // TODO
        // hit the GAR PR server and see if the user's regions have been updated more recently
        // than ours. if so, download the user's region players / rankings and then display a
        // notification to the user.
    }


}
