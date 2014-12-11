package com.garpr.android.misc;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.activities.RankingsActivity;
import com.garpr.android.data.Settings;
import com.garpr.android.models.Region;


/**
 * Much of this class's code was taken from the Android documentation:
 * https://developer.android.com/training/notify-user/build-notification.html
 */
public final class Notifications {


    private static final int NOTIFICATION_ID = 1;




    public static void clear() {
        final NotificationManager nm = getNotificationManager();
        nm.cancelAll();
    }


    private static NotificationManager getNotificationManager() {
        final Context context = App.getContext();
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }


    public static void showRankingsUpdated() {
        final Context context = App.getContext();
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setContentTitle(context.getString(R.string.gar_pr))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        final Region region = Settings.getRegion();
        final String contentText = context.getString(R.string.x_rankings_have_been_updated,
                region.getName());
        builder.setContentText(contentText);

        final Intent intent = new Intent(context, RankingsActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        final NotificationManager nm = getNotificationManager();
        final Notification notification = builder.build();
        nm.notify(NOTIFICATION_ID, notification);
    }


}
