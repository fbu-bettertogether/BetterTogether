package com.example.bettertogether;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.bettertogether.models.Group;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 1123;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getBundleExtra("bundle");
        if (bundle != null) {
            int notificationId = intent.getIntExtra("notificationId", 0);
            createNotification(context, "BetterTogether", "Check out this new gorup!", "Alert", notificationId);
            Group group = (Group) bundle.getSerializable("group");
            Intent i = new Intent(context, WeeklyIntentService.class);
            i.putExtra("bundle", bundle);
            WeeklyIntentService.enqueueWork(context, i);
        } else {
            Log.e("alarmReceiver", "group was null");
            return;
        }
    }

    public void createNotification(Context context, String msg, String msgTxt, String msgAlert, int notifId) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "Better Together Group Notifications";
        String content = "Check out this user:" + ParseUser.getCurrentUser().getUsername() + "'s new post.";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String title = "BetterTogether";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.handshake)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
        notificationBuilder.setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(notifId /* ID of notification */, notificationBuilder.build());
    }
}
