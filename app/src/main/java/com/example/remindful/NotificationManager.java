package com.example.remindful;

public class NotificationManager {

    private static int NumOfActiveNotis=0;

    public NotificationManager(){
        //Check Perms

        //Grab Noti Channel

        //Grab main channel
    }

    protected void NotificationBuilder(){
        //Args => temp builder -- return builder??
    }

    protected void BuildNotification(){
        NumOfActiveNotis++;
        return;
    }
    protected void DestroyNotification(){
        NumOfActiveNotis--;
        return;
    }
    protected void DestroyAllNotifications(){
        NumOfActiveNotis=0;
    }
}
