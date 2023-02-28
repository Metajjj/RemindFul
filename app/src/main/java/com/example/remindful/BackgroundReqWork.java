package com.example.remindful;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BackgroundReqWork extends Worker {
    private Context context;

    public BackgroundReqWork(Context con, WorkerParameters WP){
        super(con,WP);

        context=con; //constructor fires when dowork is fired
    }

    @NonNull
    @Override
    public Result doWork() {
        //setForegroundAsync(getForegroundInfo()); //For expedite
        final DatabaseHandler DH = new DatabaseHandler(context);

        //System.out.println("Id: "+getId()+"\nWorker KvP: "+getInputData().getKeyValueMap());
        //WORKS

        NotificationManagerCust NMC = new NotificationManagerCust(context);

        if ( !getInputData().getString("LinkageID").isEmpty() && getInputData().getString("LinkageID") != null ) {
            int LinkID = Integer.parseInt(getInputData().getString("LinkageID"));
            NMC.DestroyNotification(null, LinkID); //removes noti on background fired

            ContentValues CV = new ContentValues();
            CV.putNull(DH.R_TIME);
            DH.getWritableDatabase().update(DH.DBname, CV, DH.ID + " = ?", new String[]{String.valueOf(LinkID - 1)});
            //turn R_time to null and update noti

            System.out.println(
                    DH.CursorSorter(
                            DH.getReadableDatabase().query(DH.DBname, null, DH.ID + " = ?", new String[]{String.valueOf(LinkID - 1)}, null, null, null)
                    )
            );


            //StartActivity -- NewNote w data
            Looper.prepare();

            //System.out.println("Activity launch");
            context.getApplicationContext().startActivity(new Intent(context, NewNote.class).putExtra("i",  DH.CursorSorter(DH.getReadableDatabase().query(DH.DBname, null, DH.ID + " = ?", new String[]{String.valueOf(LinkID - 1)}, null, null, null)).get(0)
                            ).setComponent(new ComponentName(context.getPackageName(), NewNote.class.getName()))
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );
            //Works with intent filters in manifest
        }

        DH.close();
        new NotificationManagerCust(context); //Update main noti
        return Result.success();
    }
}
