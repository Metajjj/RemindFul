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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

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
        try{ DataExists( (HashMap<String,String>) getIntent().getExtras().get("i")); } catch (Exception e){}
    }
    private void DataExists(HashMap<String,String> S){
        DataExist = true;
        ((TextView)findViewById(R.id.NewNoteTitle)).setText("Update Note");

        ((TextView)findViewById(R.id.NewNoteNoteDetail)).setText( S.get(DH.NOTE) );
        ((TextView)findViewById(R.id.NewNoteNoteTitle)).setText( S.get(DH.TITLE) );

        G_YMDHMS = S.get(DH.YMDHMS);
        G_ID = S.get(DH.ID);
    }

    public void Remind(View v){
        CheckBox b = (CheckBox)v;
        ((TextView)findViewById(R.id.NewNoteSave)).setText( (b.isChecked()? "SAVE & REMIND" : "SAVE") );
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

    public void Save(View v){
        //On click.. background flashes green / toast popup to say saved
        String Saved;
        v.setOnClickListener(null);

        findViewById(R.id.NewNoteRemindBox).setOnClickListener(null);

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

            findViewById(R.id.NewNoteRemindBox).setOnClickListener(this::Remind);
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

            // TODO On save => reload into update ??
        }
    }

    private void Remind(){
        //Set new activity... do stuff.. grab R_Time //If no diff between year/day/ wutevs.. dont convert to sec and ignore
        //Have to declare bundle outside



        ArrayList<HashMap<String,String>> AL = DH.Readquery(MessageFormat.format(
                "SELECT * FROM `{0}` ORDER BY `{1}` DESC ;",
                DH.DBname, DH.YMDHMS
        ));

        //System.out.println(AL.size()+"\n"+AL );

        Bundle b = new Bundle(); b.putSerializable("HashMap",AL.get(0));

        getSupportFragmentManager().beginTransaction().replace(R.id.NewNoteFragHolder,RemindFragment.class, b ).commit();

        findViewById(R.id.NewNoteFragHolder).bringToFront();
    }

    private void Update(){
        String title=((TextView)findViewById(R.id.NewNoteNoteTitle)).getText().toString();
        String note=((TextView)findViewById(R.id.NewNoteNoteDetail)).getText().toString();

        //String Query = MessageFormat.format("UPDATE `{0}` SET `{1}` = \"{2}\", `{3}` = \"{4}\", `{5}` = {6} WHERE `{7}` = {8} AND `{9}` = {10}",DH.DBname, DH.TITLE, title, DH.NOTE, note, DH.YMDHMS, CalYMDHMS(), DH.ID, G_ID, DH.YMDHMS, G_YMDHMS);
        //new Home().WriteLine(Query+"\n"+getIntent().getExtras().get("i"));
        //DH.Writequery(Query);

        ContentValues CV = new ContentValues(); CV.put(DH.TITLE,title);CV.put(DH.NOTE,note);CV.put(DH.YMDHMS,CalYMDHMS());
        DH.getWritableDatabase().update(DH.DBname,CV,MessageFormat.format("{0}=? AND {1}=?",DH.ID,DH.YMDHMS),new String[]{G_ID,G_YMDHMS});
    }

}


