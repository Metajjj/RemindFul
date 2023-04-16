package com.example.remindful;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotiActionHandler extends BroadcastReceiver {

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        System.out.println("Receiver!");

        if( !intent.getExtras().isEmpty() && intent.getExtras().getString("D1").equals("RemindFulNoti")){
            //Do magic
            System.out.println("D1: "+intent.getExtras().getString("D1")+" NotiAction");
        }else if( !intent.getExtras().isEmpty() && intent.getExtras().getString("D1").equals("RemindFulMAINNoti")){
            //Do magic
            System.out.println("D1: "+intent.getExtras().getString("D1")+" NotiMAINAction");
        }
    }
}
