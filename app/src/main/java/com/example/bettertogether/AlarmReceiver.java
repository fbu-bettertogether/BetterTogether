package com.example.bettertogether;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.bettertogether.models.Group;

import org.parceler.Parcels;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 1123;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getBundleExtra("bundle");
        if (bundle != null) {
            Group group = (Group) bundle.getSerializable("group");
            Intent i = new Intent(context, WeeklyIntentService.class);
            i.putExtra("bundle", bundle);
            WeeklyIntentService.enqueueWork(context, i);
        } else {
            Log.e("alarmReceiver", "group was null");
            return;
        }
    }
}
