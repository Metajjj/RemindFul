package com.example.remindful;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;
import androidx.work.WorkManager;

import java.util.ArrayList;
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

                                                        //ContentValue set null
                ContentValues CV = new ContentValues(); CV.putNull(DH.R_TIME);
                DH.getWritableDatabase().update(DH.DBname,CV,DH.ID+"=?",new String[]{String.valueOf(intent.getExtras().getInt("LinkageID")-1)});

            } else if(intent.getExtras().getString("Code").equals("HIDE")) {
                //Hide noti but dont cancel remind
                NotificationManagerCompat.from(context).cancel(null, intent.getExtras().getInt("LinkageID"));
            }
        } else if( !intent.getExtras().isEmpty() && intent.getExtras().getString("D1").equals("RemindFulMAINNoti")){

            //Delete all background workers and Notis
            if(intent.getExtras().getString("Code").equals("CANCELALL")) {
                NMC.DestroyAllNotifications();
                WorkManager.getInstance(context).cancelAllWork();

                //KILL APP PROCESS (background)
                android.os.Process.killProcess(android.os.Process.myPid());

            } else if (intent.getExtras().getString("Code").equals("SHOWALL")) {
                //code to show setup notis.. foreach based on DB + r_times vs curr time
                ///Del all notis then readd??

                //Not allowed null as selectionArg
                ArrayList<HashMap<String, String>> CurrNotes = DH.CursorSorter(
                        DH.getReadableDatabase().query(DH.DBname,new String[]{DH.ID,DH.TITLE,DH.R_TIME},DH.R_TIME+" IS NOT NULL",null,null,null,null)
                );

                //Loops all not null R_times and sets down notis
                for (HashMap<String,String> CurrNote: CurrNotes ) {
                    int LinkID = Integer.valueOf(CurrNote.get(DH.ID))+1;
                    NMC.BuildNotification(
                            NMC.NotificationBuilder(CurrNote.get(DH.TITLE), "Expand to see snippet of note!", CurrNote.get(DH.NOTE),
                                    new Object[]{"Cancel Remind", PendingIntent.getBroadcast(context, LinkID * -1, new Intent(context, NotiActionHandler.class).putExtra("LinkageID", LinkID).putExtra("D1", "RemindFulNoti").putExtra("Code", "CANCEL"), PendingIntent.FLAG_MUTABLE)},
                                    new Object[]{"Hide Noti (not cancel)", PendingIntent.getBroadcast(context, LinkID, new Intent(context, NotiActionHandler.class).putExtra("LinkageID", LinkID).putExtra("D1", "RemindFulNoti").putExtra("Code", "HIDE"), PendingIntent.FLAG_MUTABLE)},
                                    null
                            )
                            , null
                            , LinkID
                    );
                }
            }
        }
        System.out.println(intent.getExtras().getString("Code"));
        DH.close();
    }
}
