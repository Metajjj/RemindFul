package com.example.remindful;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class NewNote extends AppCompatActivity {
    private String G_ID,G_YMDHMS; private Boolean DataExist=false;

    private void GrabTheme(){
        try {
            new Home().SetupThemeList();

            BufferedReader bfr = new BufferedReader( new FileReader( new File(getApplicationContext().getFilesDir(), "F")) );
            String l = bfr.readLine();
            //System.out.println(MessageFormat.format( "Read: {0} : ID: {1} | CurrThemeID: {2}", l, getResources().getIdentifier(l, "style", getPackageName() ) , Home.Themes.get(Home.ThemeNum) ));
            bfr.close();

            //Find resID via name
            Home.ThemeNum = Home.Themes.indexOf( getResources().getIdentifier(l, "style", getPackageName() ) );

            //If theme doesnt exist i.e. changed name
            Home.ThemeNum = (Home.Themes.get(Home.ThemeNum) >= 0) ? Home.ThemeNum : 0; //If exist, return it else make it start from 0  -- new theme auto written into file

        }catch (Exception e){ System.err.println("Err w bfr? "+e); }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //Grab from file so that it works even if launched as activity
        GrabTheme();

        setTheme(Home.Themes.get(Home.ThemeNum));

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header
        setContentView(R.layout.newnote);

        //check for extra bundle intent crap and load it up === INSTERT => UPDATE colInfo /ALTER tableInfo
        try{ DataExists( (HashMap<String,String>) getIntent().getExtras().get("i")); } catch (Exception e){}
    }

    //Setting custom anims for each activity fired
    @Override
    public void startActivity(Intent i) { super.startActivity(i); overridePendingTransition(R.anim.activity_in,R.anim.activity_out); }
    @Override
    public void startActivity(Intent i, @Nullable Bundle o) { super.startActivity(i, o); overridePendingTransition(R.anim.activity_in,R.anim.activity_out); }

    @Override
    protected void onStart() {
        super.onStart();

        findViewById(R.id.NewNoteCheckBox).setOnClickListener(this::Remind);
        findViewById(R.id.NewNoteSave).setOnClickListener(this::Save);
    }

    private void DataExists(HashMap<String,String> S){
        DatabaseHandler DH = new DatabaseHandler(getApplicationContext());

        DataExist = true;
        ((TextView)findViewById(R.id.NewNoteTitle)).setText("Update Note");

        ((TextView)findViewById(R.id.NewNoteNoteDetail)).setText( S.get(DH.NOTE) );
        ((TextView)findViewById(R.id.NewNoteNoteTitle)).setText( S.get(DH.TITLE) );

        G_YMDHMS = S.get(DH.YMDHMS);
        G_ID = S.get(DH.ID);

        DH.close();
    }

    private void Remind(View v){
        CheckBox b = (CheckBox)v;
        ((TextView)findViewById(R.id.NewNoteSave)).setText( (b.isChecked() ? "SAVE & REMIND" : "SAVE") );
    }

    protected String CalYMDHMS(){
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

    private void Save(View v){
        //On click.. background flashes green / toast popup to say saved
        TextView tv = (TextView) v;
        String Saved;
        v.setOnClickListener(null);

        findViewById(R.id.NewNoteCheckBox).setOnClickListener(null);

        if( tv.getText().toString().equals("SAVE") ){

            TrueSave();

            Saved="SAVED";
        }else{

            TrueSave(); Remind();

            Saved="SAVED & REMINDED";
        }

        String tv2 = tv.getText().toString();
        tv.setText("✓"+Saved+"✓");
        //32C95E
        tv.setBackgroundColor(getResources().getColor(R.color.GreenTick));

        new Handler().postDelayed(() -> {
            tv.setText(tv2);
            tv.setBackgroundResource(R.drawable.roundbordernote);
            v.setOnClickListener(this::Save);

            findViewById(R.id.NewNoteCheckBox).setOnClickListener(this::Remind);

            },1300);
    }

    private void TrueSave(){
        DatabaseHandler DH = new DatabaseHandler(getApplicationContext());

        if(DataExist){
            Update();
        } else {
            //DH.Writequery( DH.InsertBuilder(DH.DBname, new String[]{DH.YMDHMS, DH.TITLE, DH.NOTE}, new String[][]{{CalYMDHMS(), "\"" + ((TextView) findViewById(R.id.NewNoteNoteTitle)).getText().toString() + "\"", "\"" + ((TextView) findViewById(R.id.NewNoteNoteDetail)).getText().toString() + "\""}} ) );

            ContentValues CV = new ContentValues(); //Cant be single line
            CV.put(DH.YMDHMS,CalYMDHMS()); CV.put(DH.TITLE,((TextView) findViewById(R.id.NewNoteNoteTitle)).getText().toString()); CV.put(DH.NOTE,((TextView) findViewById(R.id.NewNoteNoteDetail)).getText().toString()); CV.put(DH.ID,DH.getAutoIncrement());
            //DH.getWritableDatabase().insert(DH.DBname,null,CV);
            DH.Insert(DH.DBname,null,CV);

            HashMap<String,String> HM = DH.CursorSorter(
                DH.getReadableDatabase().query(DH.DBname,null,MessageFormat.format(
                        "`{0}` = ? AND `{1}` = ?",
                        DH.TITLE,DH.NOTE
                ),new String[]{((TextView) findViewById(R.id.NewNoteNoteTitle)).getText().toString(),((TextView) findViewById(R.id.NewNoteNoteDetail)).getText().toString()},null,null,"`"+DH.ID+"` DESC")
            ).get(0);

            new Handler().postDelayed(()->{
                //startActivity(new Intent(this, NewNote.class).putExtra("i", (HashMap<String,String>) HM)); this.finish();
                DataExists(HM);
            },1300);
        }

        DH.close();
    }

    private void Remind(){
        DatabaseHandler DH = new DatabaseHandler(getApplicationContext());
        //Have to declare bundle outside

        ArrayList<HashMap<String,String>> AL = DH.CursorSorter( DH.getReadableDatabase().query(DH.DBname,null,null,null,null,null,DH.YMDHMS+" DESC"));

        //System.out.println(AL.size()+"\n"+AL );

        Bundle b = new Bundle(); b.putSerializable("HashMap",AL.get(0));

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.frag_in, R.anim.frag_out)
                .replace(R.id.NewNoteFragHolder,RemindFragment.class, b ).commit();

        findViewById(R.id.NewNoteFragHolder).bringToFront();

        DH.close();
    }

    private void Update(){
        DatabaseHandler DH = new DatabaseHandler(getApplicationContext());

        String title=((TextView)findViewById(R.id.NewNoteNoteTitle)).getText().toString();
        String note=((TextView)findViewById(R.id.NewNoteNoteDetail)).getText().toString();

        ContentValues CV = new ContentValues(); CV.put(DH.TITLE,title);CV.put(DH.NOTE,note);CV.put(DH.YMDHMS,CalYMDHMS());
        DH.getWritableDatabase().update(DH.DBname,CV,MessageFormat.format("{0}=? AND {1}=?",DH.ID,DH.YMDHMS),new String[]{G_ID,G_YMDHMS});

        DH.close();
    }

    @Override
    public void onBackPressed() {
        System.out.println("Back key pressed!");

        //Check if any frag open
        for(Fragment Frag : getSupportFragmentManager().getFragments()) {
            //System.out.println("NumOfFrags: "+getSupportFragmentManager().getFragments().size()+" | Frag up: "+Frag.isAdded()+" | ID:"+Frag.getId());
            if (Frag.isVisible()){
                System.out.println("REMOVING FRAG");
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.frag_in,R.anim.frag_out).remove(Frag).commit();
                return;
            }
        }
        System.out.println("Backing activity");
        startActivity(new Intent(this, Home2.class));
        overridePendingTransition(R.anim.activity_in,R.anim.activity_out);
    }
}


