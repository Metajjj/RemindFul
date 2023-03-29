package com.example.remindful;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Wut to do when received alarm
        Toast.makeText(context, "Alarm fired!", Toast.LENGTH_SHORT).show();
    }
}
