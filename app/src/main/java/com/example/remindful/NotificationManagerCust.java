package com.example.remindful;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationManagerCust {

    private Context context;
    private final String NotiChannelID="RemindFul_NotiID";

    public static HashMap<String,HashMap<String,String>> LinkageID_Note = new HashMap<>();

    //Mini notis run a backgroundworker to kill certain background workers ??

    public NotificationManagerCust(Context context){
        this.context = context;
        //Check Perms
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
        {
            Toast.makeText(context.getApplicationContext(), "Need perm for notis!", Toast.LENGTH_SHORT).show();

        }

        //Setting up NotiChannel for app if doesnt exist
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel(NotiChannelID, NotiChannelID, NotificationManager.IMPORTANCE_DEFAULT));
        }

        MainNotiUpdate();
    }

    protected NotificationCompat.Builder NotificationBuilder(String Title, String SmallText , String ExpandText, @Nullable Object[] A1, @Nullable Object[] A2, @Nullable Object[] A3){
        String Atitle; PendingIntent Aintent;

        //Cancel noti action -- go to noti action : has to be id only iff updated
        //Args => temp builder -- return builder??

        NotificationCompat.Builder NBS = new NotificationCompat.Builder(context, NotiChannelID)
                .setContentTitle(Title)
                .setContentText(SmallText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(ExpandText))
                .setAutoCancel(false).setContentIntent(null)
                .setSmallIcon(R.drawable.cm)
                .setOngoing(true)

                //.addAction(0,"Test PendInt",PendingIntent.getActivity(context,0,new Intent(context,Home2.class),PendingIntent.FLAG_IMMUTABLE)) //WORKS
                ;


        if (A1 != null){
            try {
                Atitle = A1[0].toString(); Aintent= (PendingIntent) A1[1];
                NBS.addAction(0, Atitle, Aintent);
            } catch (Exception e){ }
        }

        if (A2 != null){
            try {
                Atitle = A2[0].toString(); Aintent= (PendingIntent) A2[1];
                NBS.addAction(0, Atitle, Aintent);
            } catch (Exception e){ }
        }

        if (A3 != null){
            try {
                Atitle = A3[0].toString(); Aintent= (PendingIntent) A3[1];
                NBS.addAction(0, Atitle, Aintent);
            } catch (Exception e){ }
        }

        return NBS;
    }

    protected void BuildNotification(NotificationCompat.Builder NotiSetting, @Nullable String Tag, int ID){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) { throw new Error("Permission missing!"); }

        NotificationManagerCompat.from(context).notify(Tag,ID,NotiSetting.build());

        MainNotiUpdate();
    }
    protected void DestroyNotification(@Nullable String Tag, int ID) {
        NotificationManagerCompat.from(context).cancel(Tag, ID);
         //No error from being called on one that doesnt exist

        MainNotiUpdate(); //updates slowly??
    }

    protected void DestroyAllNotifications(){
        NotificationManagerCompat.from(context).cancelAll();

    }

    protected void MainNotiUpdate(){

        //update main noti -- no deconstructors!
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(context,"NEED NOTI PERM!",Toast.LENGTH_LONG).show();
            return;
        }

        //Get number of things w R_time (that are being reminded for)
        final DatabaseHandler DH = new DatabaseHandler(context);
        ArrayList<HashMap<String, String>> CurrNotes = DH.CursorSorter(
                DH.getReadableDatabase().query(DH.DBname,new String[]{DH.ID,DH.TITLE,DH.R_TIME},DH.R_TIME+" IS NOT NULL",null,null,null,null)
        );

        // IF no reminding.. doesnt need noti!
        if (CurrNotes.size() <= 0 ){ return; }

        //Main Noti Settings
        NotificationCompat.Builder NBS = new NotificationCompat.Builder(context,NotiChannelID)
                .setSmallIcon(R.drawable.cm) //Small Icon for noti that goes in top left
                .setContentTitle( /*(Destroyable) ? "Swipe to Remove" :*/ "Main Notification" ) //Noti title
                .setContentText( /*(Destroyable) ? null :*/ "Upcoming notifications/reminders (+ hidden): "+CurrNotes.size()) //Collapsed Noti txt
                .setPriority(NotificationCompat.PRIORITY_HIGH) //Priority ?
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("Upcoming notifications/reminders: "+NumOfActiveNotis+"\nDON'T CLOSE APP FOR REMINDING TO WORK!")) //Expanded noti text
                .setAutoCancel(false) //Anytap on noti = cancel/remove noti
                .setContentIntent(null) //Starts new activity when clicked - Default click
                .addAction(0,"Cancel All Noti & Remind",
                        PendingIntent.getBroadcast(context,Integer.MAX_VALUE, new Intent(context,NotiActionHandler.class).putExtra("D1","RemindFulMAINNoti").putExtra("Code","CANCELALL"),PendingIntent.FLAG_MUTABLE)
                )
                .addAction(0,"Show All",
                        PendingIntent.getBroadcast(context, Integer.MIN_VALUE,new Intent(context,NotiActionHandler.class).putExtra("D1","RemindFulMAINNoti").putExtra("Code","SHOWALL"),PendingIntent.FLAG_MUTABLE)
                )
                .setOngoing(true) //Stops notification being swiped away
        ;

        NotificationManagerCompat.from(context).notify(0,NBS.build());

        DH.close();
        return;
    }
}
