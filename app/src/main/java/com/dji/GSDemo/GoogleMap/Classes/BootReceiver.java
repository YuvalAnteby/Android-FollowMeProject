package com.dji.GSDemo.GoogleMap.Classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import android.widget.Toast;

/**
 * BroadcastReceiver to start the service in the foreground if the phone has been restarted
 */
public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

        //code to execute when Boot Completd

        Intent i = new Intent(context, NotificationService.class);
            ContextCompat.startForegroundService(context,i);

        Toast.makeText(context, "Booting Completed", Toast.LENGTH_LONG).show();

    }
}
