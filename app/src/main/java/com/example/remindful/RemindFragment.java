package com.example.remindful;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class RemindFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Has to run before creation else error
        SetupPermGrabber();

        super.onCreateView(inflater, container, savedInstanceState);

        if(getArguments() != null && getArguments().getSerializable("HashMap") == null){
            Toast.makeText(getContext(), "Error detecting note for reminding!!", Toast.LENGTH_SHORT).show();
            RemFragCloseFrag(new View(getContext()));
        }

        CurrNote = (HashMap<String, String>) getArguments().getSerializable("HashMap");

        context = getContext().getApplicationContext();

        return inflater.inflate(R.layout.remind_fragment, container, false);
    }

    android.content.Context context;

    private HashMap<String,String> CurrNote;

    private
    ActivityResultLauncher<String> ARL;

    private void SetupPermGrabber(){
        //New way of checking permission - has to be created before fragment is
        ARL = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                res -> {
                    if (!res) {
                        //Not granted!
                        Toast.makeText(context, "Need perm to work!", Toast.LENGTH_LONG).show();
                        RemFragCloseFrag(new View(context));
                    }
                }
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        final DatabaseHandler DH = new DatabaseHandler(context);
        getActivity().findViewById(R.id.RemFragBg).setOnClickListener(this::RemFragCloseFrag);
        getActivity().findViewById(R.id.RemFragMainCont).setOnClickListener(null);
        //MainCont click activates Bg click if not null

        getActivity().findViewById(R.id.RemFragTimeHour).setOnFocusChangeListener(this::RemFragHourCheck); //HOUR
        getActivity().findViewById(R.id.RemFragTimeMin).setOnFocusChangeListener(this::RemFragMinCheck); //MIN
        getActivity().findViewById(R.id.RemFragTimeSec).setOnFocusChangeListener(this::RemFragSecCheck);//SEC

        getActivity().findViewById(R.id.RemFragDateYear).setOnFocusChangeListener(this::RemFragYearCheck); //YEAR
        getActivity().findViewById(R.id.RemFragDateMonth).setOnFocusChangeListener(this::RemFragMonthCheck); //MONTH
        getActivity().findViewById(R.id.RemFragDateDay).setOnFocusChangeListener(this::RemFragDayCheck); //DAY

        getActivity().findViewById(R.id.RemFragButt).setOnClickListener(this::RemFragFinalCheck);

        //DatePickerDialog  NEEDS API 24 min
        getActivity().findViewById(R.id.RemFragDateButt).setOnClickListener((v)->{
            //Custom layout?
        });

        //TimePickerDialog
        getActivity().findViewById(R.id.RemFragTimeButt).setOnClickListener((v)->{
            TimePickerDialog TPD = new TimePickerDialog(context, (timePicker, H, M) -> {
                String Hour=H+"",Min=M+"";
                System.out.println("Hour: "+H+"  Min: "+M);
                Hour = (Hour.length()<2) ? "0"+Hour : Hour;
                Min = (Min.length()<2) ? "0"+Min : Min;

                DDInterSet(R.id.RemFragTimeHour,Hour+"");
                DDInterSet(R.id.RemFragTimeMin,Min+"");
            },
                    (DDInterGet(R.id.RemFragTimeHour).equals("")) ? Calendar.getInstance().get(Calendar.HOUR_OF_DAY) : Integer.parseInt( DDInterGet(R.id.RemFragTimeHour) ),
                    Calendar.getInstance().get(Calendar.MINUTE),
                    true);
            TPD.setButton(DialogInterface.BUTTON_POSITIVE,"Confirm Time",TPD);
            TPD.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel",TPD);
            TPD.show();
        });
    }

    private void DDInterSet(int ResID, String Txt){
        //Need to use this else stringRes err

        switch(ResID){
            case R.id.RemFragTimeHour:
                Txt = ( Integer.parseInt(Txt) < 24 ) ? Txt : "";
                break;
            case R.id.RemFragTimeMin:
                Txt = ( Integer.parseInt(Txt) < 60 ) ? Txt : "";
                break;
            case R.id.RemFragDateYear:
                Txt = ( Integer.parseInt(Txt) >= Calendar.getInstance().get(Calendar.YEAR) ) ? Txt : "";
                break;
            case R.id.RemFragDateMonth:
                Txt = ( Integer.parseInt(Txt) <= 12 ) ? Txt : "";
                break;
            case R.id.RemFragDateDay:
                Txt = ( Integer.parseInt(Txt) <= 31 ) ? Txt : "";
                break;
        }

        ((TextView)getActivity().findViewById(ResID)).setText(Txt);
    }
    private String DDInterGet(int ResID){
        return ((TextView)getActivity().findViewById(ResID)).getText() +"";
    }

    public void RemFragHourCheck(View v, boolean isFocused){
        //Guarantee check
        if (isFocused || ((TextView)v).getText().toString().equals("")){ return; }
        int h = Integer.parseInt( ((TextView)v).getText()+"" );
        String H = String.valueOf(h);
        h = (h==24) ? 0 : h;
        H = (H.length()<2) ? "0"+H : H;
        //Check...
        ((TextView) v).setText( (h<=23 && h>=0) ? H : "");
        //Reset if wrong
    }

    public void RemFragMinCheck(View v, boolean isFocused){
        //Guarantee check
        if (isFocused || ((TextView)v).getText().toString().equals("")){ return; }
        int m = Integer.parseInt( ((TextView)v).getText()+"" );
        m = (m==60) ? 0 : m;
        //Check...
        if(! (m<=59 && m>=0) ){ ((TextView) v).setText(""); } else{
            ((TextView) v).setText( ((String.valueOf(m).length()<2) ? "0"+m : m+"") );
        }
        //Reset if wrong
    }

    public void RemFragYearCheck(View v, boolean isFocused){
        if (isFocused || ((TextView)v).getText().toString().equals("")){ return; }
        int y = Integer.parseInt( ((TextView)v).getText()+"" );
        String Y = String.valueOf(y);

        if( String.valueOf(y).length()<4 ){
            y = Integer.parseInt( String.valueOf(Calendar.getInstance().get(Calendar.YEAR)).substring(0,4-Y.length()) + Y );
        }

        ((TextView)v).setText( (y>=Calendar.getInstance().get(Calendar.YEAR) ) ? y+"" : "" );
    }

    public void RemFragMonthCheck(View v, boolean isFocused){
        if (isFocused || ((TextView)v).getText().toString().equals("")){ return; }
        int m = Integer.parseInt( ((TextView)v).getText()+"" );
        String M = String.valueOf(m);
        M = ( M.length()<2 ) ? "0"+M : M;

        ((TextView)v).setText( (m>=1 && m<=12) ? M : "" );
    }

    public void RemFragDayCheck(View v, boolean isFocused){
        if (isFocused || ((TextView)v).getText().toString().equals("")){ return; }
        int d = Integer.parseInt( ((TextView)v).getText()+"" );
        String D = String.valueOf(d);
        D = ( D.length()<2 ) ? "0"+D : D;

        ((TextView)v).setText( (d>=1 && d<=31) ? D : "" );
    }

    public void RemFragSecCheck(View v, boolean isFocused){
        //Guarantee check
        if (isFocused || ((TextView)v).getText().toString().equals("")){ return; }
        int s = Integer.parseInt( ((TextView)v).getText()+"" );
        s = (s==60) ? 0 : s;
        //Check...
        if(! (s<=59 && s>=0) ){ ((TextView) v).setText(""); } else{
            ((TextView) v).setText( ((String.valueOf(s).length()<2) ? "0"+s : s+"") );
        }
        //Reset if wrong
    }

    private void RemFragFinalCheck(View v){
        TextView tv = (TextView)v;
        String txt = tv.getText().toString();
        tv.setText("✓ Reminder set ✓");
        //32C95E
        tv.setBackgroundColor(Color.rgb(50,201,94));

        new Handler().postDelayed(()->{
            tv.setText(txt);
            tv.setBackgroundResource(R.drawable.roundbordernote);
        },1300);

        TextView Hour = getActivity().findViewById(R.id.RemFragTimeHour), Min = getActivity().findViewById(R.id.RemFragTimeMin), Year = getActivity().findViewById(R.id.RemFragDateYear), Month = getActivity().findViewById(R.id.RemFragDateMonth), Day = getActivity().findViewById(R.id.RemFragDateDay), Sec = getActivity().findViewById(R.id.RemFragTimeSec);

        //Forcefully run all checks to make sure all data is as valid as can be
        RemFragHourCheck(Hour,false);
        RemFragMinCheck(Min,false);
        RemFragSecCheck(Sec,false);
        RemFragDayCheck(Day,false);
        RemFragMonthCheck(Month,false);
        RemFragYearCheck(Year,false);

        if( Day.getText().toString().equals("") || Month.getText().toString().equals("") || Year.getText().toString().equals("") || Min.getText().toString().equals("") || Hour.getText().toString().equals("") || Sec.getText().toString().equals("") ){
            Toast.makeText(context, "Date/Time input missing!\nNo reminding!", Toast.LENGTH_SHORT).show();
            return;
        }

        System.out.println("CONFIRM REMIND");
        SetupRemindWorker(Year.getText().toString() + Month.getText().toString() + Day.getText().toString() + Hour.getText().toString() + Min.getText().toString() + Sec.getText().toString());
    }

    private void SetupRemindWorker(String Datetime){
        //System.out.println(Datetime);
        Calendar cal = Calendar.getInstance();
        java.text.DateFormat DF = new SimpleDateFormat("yyyyMMddHHmmss");
        DF.setLenient(false);
        //check if Date given is feasible via calender
        try {
            DF.parse(Datetime);

            //Make sure Date is not past of curr date
            long CurrTime = cal.getTimeInMillis();
            cal.setTime(DF.parse(Datetime));
            //System.out.println(MessageFormat.format( "Curr: {0} | Inp: {1} | Res: {2}", cal.getTimeInMillis() ));
            if (CurrTime >= cal.getTimeInMillis()){ throw new Exception("Date in past!"); }

        } catch (Exception e) {
            System.out.println("Err: "+e);
            Toast.makeText(context, "Disallowed date/time detected!", Toast.LENGTH_SHORT).show();
            return;
        }
        //Given DateTime is acceptable

        //Update entry w R_TIME
        DatabaseHandler DH = new DatabaseHandler(context);
        ContentValues CV = new ContentValues(); CV.put(DH.TITLE,CurrNote.get(DH.TITLE));CV.put(DH.NOTE,CurrNote.get(DH.NOTE));CV.put(DH.YMDHMS,CurrNote.get(DH.YMDHMS));CV.put(DH.ID,CurrNote.get(DH.ID)); CV.put(DH.R_TIME,Datetime);

        DH.getWritableDatabase().update( DH.DBname, CV, MessageFormat.format("{0}=? AND {1}=? AND {2}=?",DH.ID,DH.TITLE,DH.NOTE), new String[]{CurrNote.get(DH.ID),CurrNote.get(DH.TITLE),CurrNote.get(DH.NOTE)} );

        //System.out.println(CurrNote+"|"+Datetime);

        CurrNote = (HashMap<String, String>) DH.Readquery(MessageFormat.format(
                "SELECT * FROM `{0}` WHERE `{1}`={2}",
                DH.DBname, DH.ID, CurrNote.get(DH.ID)
        )).get(0); //Regrab the new note with R_TIME

        //System.out.println(CurrNote);
        //WORKS

        long DTime, STime;
        try{
            cal.setTime( DF.parse( CurrNote.get(DH.R_TIME).toString()) );
            DTime = cal.getTimeInMillis()/1000;
            cal.setTime( DF.parse( new NewNote().CalYMDHMS() ) );
            STime = cal.getTimeInMillis()/1000;

            DTime -= STime;
            //System.out.println(DTime+"s"); ////TODO Diff of few millisecs when fired
        }catch(Exception e){}

        ////TODO FIX STRING PASSED AND UID
        //TODO setup remind worker

        //keeps running even in background
        //Expedited = run as background asap ; is important
        //Constraints = decide conditions for it to run
        //InitialDelay only works for the first time of a periodic fire, others work as interval indicated
        //BackOffCriteria takes effect when worker has to be retried - 10s min | default 30s
        //Tag as ID
        // ERR: expedited jobs cant be delayed

        //Update bgw if exists via id?? ==== ++id;
        String LinkageID = String.valueOf(Integer.parseInt(CurrNote.get(DH.ID)) +1);
        NotificationManagerCust.LinkageID_Note.put(LinkageID,CurrNote);

        Data.Builder WorkReqArgs = new Data.Builder().putString("LinkageID",LinkageID); //Cant put when build()
        for(Map.Entry<String,String> kvp : CurrNote.entrySet()){ WorkReqArgs.putString(kvp.getKey(), kvp.getValue()); }

        WorkRequest.Builder WRB = new OneTimeWorkRequest.Builder(BackgroundReqWork.class)
                .setInputData( WorkReqArgs.build() )
                //.addTag("WorkerReqTag")
                .setBackoffCriteria(BackoffPolicy.LINEAR,10, TimeUnit.SECONDS)
                .setInitialDelay(/*DTime*/20, TimeUnit.SECONDS) //When To Run - Curr Time
                .setConstraints(
                        new Constraints.Builder()
                                .setRequiresCharging(false)
                                .setRequiresBatteryNotLow(false)
                                .setRequiresDeviceIdle(false)
                                .setRequiresStorageNotLow(false)
                        .build())
                //.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST) //If expedited, no delays allowed
                ;

        //Work queries added via work request identifiers.. lets u chain them

        //enqueue to begin, unique enqueue to avoid duplication of tasks
        //Policy to change how is handled.. append, keep, replace - append => chaining tasks (fails if 1st task not success ; avoid with append_and_replace)
        WorkManager.getInstance(context).enqueueUniqueWork(LinkageID, ExistingWorkPolicy.APPEND_OR_REPLACE, (OneTimeWorkRequest) WRB.build());
        //Replace time to be reminded if updated

        //WorkManager.getInstance(context).cancelUniqueWork("UniqueNameToAvoidDups"); //Works



        //Appends or replaces if retry or fail

        //.getWorkInfo_LiveData to observe progress
        //Most live data is WorkInfos (array)
        // ERR: cannot cast lifefcycle to lifecycleowner
        /*WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData("UniqueNameToAvoidDups").observe(
                this,
                workInfosArr -> {

                    //Looper.prepare();
                    if (workInfosArr.isEmpty()) {
                        System.out.println("Empty WI arr");
                        return; //Empty err
                    }
                    //for(WorkInfo workInfos : workInfosArr){ }
                    WorkInfo workInfos = workInfosArr.get(0); //Only 1 UID

                    System.out.println("WorkState: "+workInfos.getState()+" | String: "+workInfos+" |ID:"+workInfos.getId());

                    if (workInfos.getState() == WorkInfo.State.SUCCEEDED) {
                        new Handler().postDelayed(() -> {
                            Toast.makeText(context, "WorkReqSucceed!!", Toast.LENGTH_SHORT).show();
                            //setview => custom toast
                            System.out.println("WRS : "+workInfos.getOutputData().getString("D1"));
                        }, 5000);
                    }
                }
        );*/ //Wont use observer - reliant on state active
        //WorkManager.getInstance(this).cancelUniqueWork("UID");
        SetupNoti(CurrNote, Integer.parseInt(LinkageID) );
    }

    private void SetupNoti(HashMap<String,String> CurrNote, int LinkID){
        //Create a Main noti here, and background fire mini notis to interact with background workers
        // - update main noti based on mini notis smhw? static int?

        final DatabaseHandler DH = new DatabaseHandler(context);

        //Setting up NotiChannel for app
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel("RemindFul_NotiID", "RemindFul_NotiID", NotificationManager.IMPORTANCE_DEFAULT));
        }

        //TODO finish off - Cancel noti, create string res:NotiID  background worker to cancel and manage intent opening.. CREATE BACKWORK HERE, CREATE NOTI AT BACKREQWORK...

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ARL.launch(Manifest.permission.POST_NOTIFICATIONS); // Check/grab perm
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

            NotificationManagerCust CustNM = new NotificationManagerCust(context);
            CustNM.BuildNotification(
                     CustNM.NotificationBuilder(CurrNote.get(DH.TITLE),"Expand to see snippet of note!",CurrNote.get(DH.NOTE),
                             new Object[]{"Cancel Remind", PendingIntent.getBroadcast(context,LinkID*-1,new Intent(context,NotiActionHandler.class).putExtra("LinkageID",LinkID).putExtra("D1","RemindFulNoti").putExtra("Code","CANCEL"),PendingIntent.FLAG_MUTABLE) },
                             new Object[]{"Hide Noti (not cancel)", PendingIntent.getBroadcast(context,LinkID,new Intent(context,NotiActionHandler.class).putExtra("LinkageID",LinkID).putExtra("D1","RemindFulNoti").putExtra("Code","HIDE"),PendingIntent.FLAG_MUTABLE) },
                             null
                     )
                    ,null
                    , LinkID
            );

        }else{
            Toast.makeText(context,"Notifications not granted and may cause crash!\nEnable and retry!",Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { ARL.launch(Manifest.permission.POST_NOTIFICATIONS); }
        }
    }

    public void RemFragCloseFrag(View v){

        getParentFragmentManager().beginTransaction().remove(RemindFragment.this).commit();
        //getActivity().findViewById(R.id.home2FragHolder).back
        //startActivity(new Intent(context,Home2.class));
    }
}
