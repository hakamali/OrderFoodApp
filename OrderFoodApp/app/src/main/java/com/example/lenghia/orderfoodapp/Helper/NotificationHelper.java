package com.example.lenghia.orderfoodapp.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.example.lenghia.orderfoodapp.R;

public class NotificationHelper extends ContextWrapper {

    private static final String LTN_CHANEL_ID="e com.example.lenghia.orderfoodapp.LTVDev";
    private static final String LTN_CHANEL_NAME = "Order Food";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.O)// Only working this function if API >= 26
            createChanel();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChanel() {
        NotificationChannel ltnChanel = new NotificationChannel(LTN_CHANEL_ID,LTN_CHANEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        ltnChanel.enableLights(true);
        ltnChanel.enableVibration(true);
        ltnChanel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(ltnChanel);
    }

    public NotificationManager getManager() {
        if(manager == null)
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getOrderFoodChanelNotification(String title, String body, PendingIntent contentIntent, Uri soundUri)
    {
        return new Notification.Builder(getApplicationContext(),LTN_CHANEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_room_service_black_24dp)
                .setSound(soundUri)
                .setAutoCancel(false);

    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getOrderFoodChanelNotification(String title, String body, Uri soundUri)
    {
        return new Notification.Builder(getApplicationContext(),LTN_CHANEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_room_service_black_24dp)
                .setSound(soundUri)
                .setAutoCancel(false);

    }
}
