package com.garpr.android.misc;


import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.garpr.android.App;
import com.garpr.android.R;
import com.garpr.android.data.Settings;
import com.garpr.android.models.Region;


public final class Notifications {


    public static void showRankingsUpdated() {
        final Context context = App.getContext();
        final Region region = Settings.getRegion();
        final String contentText = context.getString(R.string.x_rankings_have_been_updated,
                region.getName());
        final String contentTitle = context.getString(R.string.gar_pr);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setContentText(contentText)
                .setContentTitle(contentTitle)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon() // https://developer.android.com/design/style/iconography.html#notification
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                // https://developer.android.com/training/notify-user/build-notification.html
                // https://developer.android.com/design/patterns/notifications.html
        // TODO
        // in the future use setNumber() for the notificationcompat.builder to show the user
        // their new ranking
    }


}
