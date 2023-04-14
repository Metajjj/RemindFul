package com.example.remindful;

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

        //System.out.println("Worker KvP:\n"+getInputData().getKeyValueMap());
        //WORKS


        return Result.success();
    }
}
