package com.example.remindful;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BackgroundReqWork extends Worker {
    private Context context;
    public BackgroundReqWork(Context con, WorkerParameters WP){
        super(con,WP);

        context=con;
    }

    private static int i=0; //Has to be static or if is new class open again and again by worker, infi loop
    @NonNull
    @Override
    public Result doWork() {
        //setForegroundAsync(getForegroundInfo()); //For expedite

        Looper.prepare(); //Has to be called to run handlers when returned to observer
        try {
            //getInputData().getString("D1");
            System.out.println("TOASTY : "+getInputData().getString("D1"));
            Toast.makeText(context, "Alarm fired!", Toast.LENGTH_SHORT).show();

            return Result.success(); //retry,fail
            //Fails to retry?
        }catch (Exception e){
            System.out.println("i:"+i+"\nERR: "+e);
            if(i++==3){
                return Result.failure();
            }
            return Result.retry();
        }
        //If fails, retry 3 times else give up
    }
}
