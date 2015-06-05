package com.garpr.android.sync;


import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.TaskParams;


public final class SyncManager extends GcmTaskService {


    // https://developers.google.com/android/reference/com/google/android/gms/gcm/GcmNetworkManager
    // https://developers.google.com/android/reference/com/google/android/gms/gcm/GcmTaskService


    public static void initialize() {
        // TODO
        final OneoffTask task = new OneoffTask.Builder()
                .setService(SyncManager.class)
                .build();
    }


    @Override
    public int onRunTask(final TaskParams taskParams) {
        return 0;
    }


}
