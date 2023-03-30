package com.example.remindful;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewNote extends AppCompatActivity {
    private final DatabaseHandler DH = new DatabaseHandler(NewNote.this);
    private String G_ID,G_YMDHMS; private Boolean DataExist=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header
        setContentView(R.layout.newnote);

        Remind(findViewById(R.id.NewNoteRemindBox));

        //check for extra bundle intent crap and load it up === INSTERT => UPDATE colInfo /ALTER tableInfo
        try{ DataExists( getIntent().getExtras().get("i").toString() ); } catch (Exception e){}
    }
    private void DataExists(String s){
        DataExist = true;
        ((TextView)findViewById(R.id.NewNoteTitle)).setText("Update Note");

        Matcher m1= Pattern.compile("Note:[\\s\\w\\d]+\\|").matcher(s),
        m2= Pattern.compile("Title:[\\s\\w\\d]+\\|").matcher(s),
        m3= Pattern.compile("YMDHMS:[\\s\\w\\d]+\\|").matcher(s),
        m4= Pattern.compile("ID:[\\s\\w\\d]+\\|").matcher(s);

        if (m1.find() && m2.find() && m3.find() && m4.find()){
            //G_Note = s.substring(m1.start() + "Note:".length(), m1.end() - 1);
            //G_Title = s.substring(m2.start() + "Title:".length(), m2.end() - 1);

            ((TextView)findViewById(R.id.NewNoteNoteDetail)).setText( s.substring(m1.start() + "Note:".length(), m1.end() - 1) );
            ((TextView)findViewById(R.id.NewNoteNoteTitle)).setText( s.substring(m2.start() + "Title:".length(), m2.end() - 1) );

            G_YMDHMS = s.substring(m3.start() + "YMDHMS:".length(), m3.end() - 1);
            G_ID = s.substring(m4.start() + "ID:".length(), m4.end() - 1);
        }

        //((TextView)findViewById(R.id.NewNoteNoteTitle)).setText(G_Title);
        //((TextView)findViewById(R.id.NewNoteNoteDetail)).setText(G_Note);

    }

    public void Remind(View v){
        CheckBox b = (CheckBox)v;
        ((TextView)findViewById(R.id.NewNoteSave)).setText( (b.isChecked()? "SAVE & REMIND" : "SAVE") );
    }

    private String CalYMDHMS(){
        String
        Year =""+ Calendar.getInstance().get(Calendar.YEAR),
        Month=""+ (Calendar.getInstance().get(Calendar.MONTH)+1),
        Day=""+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
        Hour=""+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        Min=""+ Calendar.getInstance().get(Calendar.MINUTE),
        Sec=""+ Calendar.getInstance().get(Calendar.SECOND);

        Month = Month.length()<2 ? "0"+Month : Month;
        Day = Day.length()<2 ? "0"+Day : Day;
        Hour = Hour.length()<2 ? "0"+Hour : Hour;
        Min = Min.length()<2 ? "0"+Min : Min;
        Sec = Sec.length()<2 ? "0"+Sec : Sec;

        return Year + Month + Day + Hour + Min + Sec;
    }

    public void Save(View v){
        //On click.. background flashes green / toast popup to say saved
        String Saved;
        v.setOnClickListener(null);

        TextView tv = (TextView) v;
        if( tv.getText() == "SAVE" ){

            TrueSave();

            Saved="SAVED";
        }else{
            Toast.makeText(this,"ERR no reminding fn!",Toast.LENGTH_SHORT).show();

            TrueSave(); Remind();

            Saved="SAVED & REMINDED";
        }

        String tv2 = tv.getText().toString();
        tv.setText("✓"+Saved+"✓");
        //32C95E
        tv.setBackgroundColor(Color.rgb(50,201,94));

        new Handler().postDelayed(() -> {
            tv.setText(tv2);
            tv.setBackgroundResource(R.drawable.roundbordernote);
            v.setOnClickListener(this::Save);
            },1300);
    }

    private void TrueSave(){

        if(DataExist){
            Update();
        } else {
            //DH.Writequery( DH.InsertBuilder(DH.DBname, new String[]{DH.YMDHMS, DH.TITLE, DH.NOTE}, new String[][]{{CalYMDHMS(), "\"" + ((TextView) findViewById(R.id.NewNoteNoteTitle)).getText().toString() + "\"", "\"" + ((TextView) findViewById(R.id.NewNoteNoteDetail)).getText().toString() + "\""}} ) );

            ContentValues CV = new ContentValues(); //Cant be single line
            CV.put(DH.YMDHMS,CalYMDHMS()); CV.put(DH.TITLE,((TextView) findViewById(R.id.NewNoteNoteTitle)).getText().toString()); CV.put(DH.NOTE,((TextView) findViewById(R.id.NewNoteNoteDetail)).getText().toString());
            DH.getWritableDatabase().insert(DH.DBname,null,CV);
        }

    }
    private void Remind(){
        //Set new activity... do stuff.. grab R_Time //If no diff between year/day/ wutevs.. dont convert to sec and ignore

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
                .setBackoffCriteria(BackoffPolicy.LINEAR,10,TimeUnit.SECONDS)
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
        WorkManager.getInstance(NewNote.this).enqueueUniqueWork("UniqueNameToAvoidDups", ExistingWorkPolicy.KEEP, (OneTimeWorkRequest) WR);

        //.getWorkInfo_LiveData to observe progress
        //Most live data is WorkInfos (array)
        // ERR: cannot cast lifefcycle to lifecycleowner
        WorkManager.getInstance(NewNote.this).getWorkInfosForUniqueWorkLiveData("UniqueNameToAvoidDups").observe(
                NewNote.this,
                workInfosArr -> {

                    if (workInfosArr.isEmpty()) {
                        return; //Empty err
                    }
                    //for(WorkInfo workInfos : workInfosArr){ }
                    WorkInfo workInfos = workInfosArr.get(0); //Only 1 UID

                    if (workInfos.getState() == WorkInfo.State.SUCCEEDED) {
                        new Handler().postDelayed(() -> {
                            Toast.makeText(this, "WorkReqSucceed!!", Toast.LENGTH_SHORT).show();
                            //setview => custom toast
                        }, 3000);
                    }
                }
        );
        //WorkManager.getInstance(this).cancelUniqueWork("UID");
    //*/

        ////FIX SET NEW FRAG TO SET R_TIME and such..
    }

    private void Noti(){
        //https://developer.android.com/develop/ui/views/notifications/build-notification#java
    }

    private void Update(){
        String title=((TextView)findViewById(R.id.NewNoteNoteTitle)).getText().toString();
        String note=((TextView)findViewById(R.id.NewNoteNoteDetail)).getText().toString();

        String Query = MessageFormat.format("UPDATE `{0}` SET `{1}` = \"{2}\", `{3}` = \"{4}\", `{5}` = {6} WHERE `{7}` = {8} AND `{9}` = {10}",DH.DBname, DH.TITLE, title, DH.NOTE, note, DH.YMDHMS, CalYMDHMS(), DH.ID, G_ID, DH.YMDHMS, G_YMDHMS);

        //new Home().WriteLine(Query+"\n"+getIntent().getExtras().get("i"));
        //DH.Writequery(Query);

        ContentValues CV = new ContentValues(); CV.put(DH.TITLE,title);CV.put(DH.NOTE,note);CV.put(DH.YMDHMS,CalYMDHMS());
        DH.getWritableDatabase().update(DH.DBname,CV,MessageFormat.format("{0}=? AND {1}=?",DH.ID,DH.YMDHMS),new String[]{G_ID,G_YMDHMS});
    }

}


