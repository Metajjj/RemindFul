package com.example.remindful;


import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;


public class RemindFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //new Home().WriteLine(""+container.getClass().getName() ); //FRAMELAYOUT TODO
        mContainer = container;

        return inflater.inflate(R.layout.remind_fragment, container, false);
    }

    ViewGroup mContainer;

    @Override
    public void onStart() {
        super.onStart();
        final DatabaseHandler DH = new DatabaseHandler(getContext());
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
            TimePickerDialog TPD = new TimePickerDialog(getContext(), (timePicker, H, M) -> {
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
            Toast.makeText(getContext(), "Date/Time input missing!\nNo reminding!", Toast.LENGTH_SHORT).show();
            return;
        }

        SetupRemindWorker(Year.getText().toString() + Month.getText().toString() + Day.getText().toString() + Hour.getText().toString() + Min.getText().toString() + Sec.getText().toString());
    }

    private void SetupRemindWorker(String Datetime){
        //System.out.println(Datetime);

        //TODO setup remind worker

        //keeps running even in background
        //Expedited = run as background asap ; is important
        //Constraints = decide conditions for it to run
        //InitialDelay only works for the first time of a periodic fire, others work as interval indicated
        //BackOffCriteria takes effect when worker has to be retried - 10s min | default 30s
        //Tag as ID
        // ERR: expedited jobs cant be delayed
        //*
        WorkRequest WR = new OneTimeWorkRequest.Builder(BackgroundReqWork.class)
                .setInputData(
                        new Data.Builder()
                                .putString("D1","SillyString")
                                .build())
                .addTag("WorkerReqTag")
                .setBackoffCriteria(BackoffPolicy.LINEAR,10, TimeUnit.SECONDS)
                .setInitialDelay(10, TimeUnit.SECONDS) //When To Run - Curr Time
                .setConstraints(
                        new Constraints.Builder()
                                .setRequiresCharging(false)
                                .build())
                //.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build();

        //Work queries added via work request identifiers.. lets u chain them

        //enqueue to begin, unique enqueue to avoid duplication of tasks
        //Policy to change how is handled.. append, keep, replace - append => chaining tasks (fails if 1st task not success ; avoid with append_and_replace)
        WorkManager.getInstance(getContext()).enqueueUniqueWork("UniqueNameToAvoidDups", ExistingWorkPolicy.KEEP, (OneTimeWorkRequest) WR);

        //.getWorkInfo_LiveData to observe progress
        //Most live data is WorkInfos (array)
        // ERR: cannot cast lifefcycle to lifecycleowner
        WorkManager.getInstance(getContext()).getWorkInfosForUniqueWorkLiveData("UniqueNameToAvoidDups").observe(
                getContext(),
                workInfosArr -> {

                    if (workInfosArr.isEmpty()) {
                        return; //Empty err
                    }
                    //for(WorkInfo workInfos : workInfosArr){ }
                    WorkInfo workInfos = workInfosArr.get(0); //Only 1 UID

                    if (workInfos.getState() == WorkInfo.State.SUCCEEDED) {
                        new Handler().postDelayed(() -> {
                            Toast.makeText(getContext(), "WorkReqSucceed!!", Toast.LENGTH_SHORT).show();
                            //setview => custom toast
                        }, 3000);
                    }
                }
        );
        //WorkManager.getInstance(this).cancelUniqueWork("UID");
    }

    public void RemFragCloseFrag(View v){

        getParentFragmentManager().beginTransaction().remove(RemindFragment.this).commit();
        //getActivity().findViewById(R.id.home2FragHolder).back
        //startActivity(new Intent(getContext(),Home2.class));
    }
}
