package com.dji.GSDemo.GoogleMap.Classes;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.dji.GSDemo.GoogleMap.Activities.ConnectionActivity;
import com.dji.GSDemo.GoogleMap.R;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

/**
 * BroadcastReceiver to show the notification at the correct time
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create or update. */
            NotificationChannel channel = new NotificationChannel("1", "We miss you!",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Please fly with us again soon");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(context, channel.getId());
        } else
            builder = new NotificationCompat.Builder(context);

        Intent myIntent = new Intent(context, ConnectionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("We miss you!")
                .setContentIntent(pendingIntent)
                .setContentText("Please fly with us again soon")
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentInfo("Info");

        notificationManager.notify(1, builder.build());
    }
}