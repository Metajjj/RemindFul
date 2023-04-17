package com.example.remindful;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;
import androidx.work.WorkManager;

import java.util.HashMap;

public class NotiActionHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final DatabaseHandler DH = new DatabaseHandler(context);
        NotificationManagerCust NMC = new NotificationManagerCust(context);

        if( !intent.getExtras().isEmpty() && intent.getExtras().getString("D1").equals("RemindFulNoti")){

            if(intent.getExtras().getString("Code").equals("CANCEL")) {
                //Cancel worker based on linkID
                WorkManager.getInstance(context).cancelUniqueWork(String.valueOf(intent.getExtras().getInt("LinkageID")));
                NMC.DestroyNotification(null, intent.getExtras().getInt("LinkageID"));
            } else if(intent.getExtras().getString("Code").equals("HIDE")) {
                //Hide noti but dont cancel remind
                NotificationManagerCompat.from(context).cancel(null, intent.getExtras().getInt("LinkageID"));
            }
        } else if( !intent.getExtras().isEmpty() && intent.getExtras().getString("D1").equals("RemindFulMAINNoti")){

            //Delete all background workers and Notis
            if(intent.getExtras().getString("Code").equals("CANCELALL")) {
                NMC.DestroyAllNotifications();
                WorkManager.getInstance(context).cancelAllWork();
            } else if (intent.getExtras().getString("Code").equals("SHOWALL")) {
                //code to show setup notis.. foreach based on DB + r_times vs curr time
                ///Del all notis then readd??
                int LinkID = intent.getExtras().getInt("LinkageID");

                HashMap<String,String> CurrNote = (HashMap<String, String>) DH.CursorSorter(
                        DH.getReadableDatabase().query(DH.DBname,new String[]{DH.ID},"*",null,null,null,null)
                ).get(0);
                DH.close();
//TODO FINISHHHHH THISSSS
                NMC.BuildNotification(
                        NMC.NotificationBuilder(CurrNote.get(DH.TITLE),"Expand to see snippet of note!",CurrNote.get(DH.NOTE),
                                new Object[]{"Cancel Remind", PendingIntent.getBroadcast(context,LinkID*-1,new Intent(context,NotiActionHandler.class).putExtra("LinkageID",LinkID).putExtra("D1","RemindFulNoti").putExtra("Code","CANCEL"),PendingIntent.FLAG_MUTABLE) },
                                new Object[]{"Hide Noti (not cancel)", PendingIntent.getBroadcast(context,LinkID,new Intent(context,NotiActionHandler.class).putExtra("LinkageID",LinkID).putExtra("D1","RemindFulNoti").putExtra("Code","HIDE"),PendingIntent.FLAG_MUTABLE) },
                                null
                        )
                        ,null
                        , LinkID
                );

            }
        }
        System.out.println(intent.getExtras().getString("Code"));
    }
}
