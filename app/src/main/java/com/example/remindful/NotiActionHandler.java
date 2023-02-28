package com.example.remindful;


import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.core.app.NotificationManagerCompat;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.HashMap;

public class NotiActionHandler extends BroadcastReceiver {
    //todo  ForegroundStart not working from lockscreen? | might be my settings

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
                NMC.DestroyAllNotifications(); //Only works within noti manager not outside

                //Turning all R_TIME to null
                ArrayList<HashMap<String, String>> Rnotes = DH.CursorSorter( DH.getReadableDatabase().query(DH.DBname,null,"`"+DH.R_TIME+"` IS NOT NULL",null,null,null,null) );
                for(HashMap s : Rnotes){
                    //System.out.println(s);
                    ContentValues CV = new ContentValues(); CV.putNull(DH.R_TIME);
                    DH.getWritableDatabase().update(DH.DBname,CV, "`"+DH.ID+"` = ?",new String[]{s.get(DH.ID)+""});
                }

                new Handler().post(()->{ if (IsAppForeground(context)){ System.out.println("FOREGROUND APP"); NMC.MainNotiUpdate(); } });
                /*//KILL APP PROCESS (background)
                new Handler().postDelayed(()->{
                    if (IsAppForeground(context)){ System.out.println("FOREGROUND APP"); NMC.MainNotiUpdate(); }
                    else {
                        System.out.println("BACKGROUND APP");
                        //NMC.MainNotiUpdate(true);
                        //NotificationManagerCompat.from(context).cancelAll();

                        //App stays in task/recent used apps - messes up theme
                        //if ( ContextCompat.checkSelfPermission(context, Manifest.permission.KILL_BACKGROUND_PROCESSES) == PackageManager.PERMISSION_GRANTED ){  android.os.Process.killProcess(Process.myPid()); }
                    }
                },1000);*/


            } else if (intent.getExtras().getString("Code").equals("SHOWALL")) {
                //code to show setup notis.. foreach based on DB + r_times vs curr time
                ///Del all notis then readd??

                //Not allowed null as selectionArg
                ArrayList<HashMap<String, String>> CurrNotes = DH.CursorSorter(
                        DH.getReadableDatabase().query(DH.DBname,new String[]{DH.ID,DH.TITLE,DH.R_TIME},DH.R_TIME+" IS NOT NULL",null,null,null,null)
                );

                //Loops all not null R_times and sets down notis
                for (HashMap<String,String> CurrNote: CurrNotes ) {
                    int LinkID = Integer.parseInt(CurrNote.get(DH.ID))+1;

                    String rtime = CurrNote.get(DH.R_TIME); //YYYYMMDDhhmmss 0-3/4-5/6-7  8-9:10-11:12-13
                    rtime = rtime.substring(0,4)+"(Y)/"+rtime.substring(4,6)+"(M)/"+rtime.substring(6,8)+"(D)  "+rtime.substring(8,10)+"h:"+rtime.substring(10,12)+"m:"+rtime.substring(12,14)+"s";

                    NMC.BuildNotification(
                            NMC.NotificationBuilder(CurrNote.get(DH.TITLE), rtime, CurrNote.get(DH.NOTE),
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
        //System.out.println(intent.getExtras().getString("Code"));
        DH.close();
    }

    protected boolean IsAppForeground(Context context){
        //Get activityManager from system service
        ActivityManager AM = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningAppProcessInfo proc : AM.getRunningAppProcesses()){
            if(proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && proc.processName.equals(context.getPackageName())){
                return true;
            }
        }
        return false;
    }

}
