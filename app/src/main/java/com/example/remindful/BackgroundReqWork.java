package com.example.remindful;

import android.content.ContentValues;
import android.content.Context;

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

        System.out.println("Id: "+getId()+"\nWorker KvP: "+getInputData().getKeyValueMap());
        //WORKS

        NotificationManagerCust NMC = new NotificationManagerCust(context);

        if ( !getInputData().getString("LinkageID").isEmpty() && getInputData().getString("LinkageID") != null ){
            int LinkID = Integer.parseInt(getInputData().getString("LinkageID"));
            NMC.DestroyNotification(null, LinkID); //removes noti on background fired

            ContentValues CV = new ContentValues(); CV.putNull(DH.R_TIME);
            DH.getWritableDatabase().update(DH.DBname,CV,DH.ID+" = ?",new String[]{""+LinkID+1});
            //TODO check this
        }


        DH.close();
        return Result.success();
    }
}
