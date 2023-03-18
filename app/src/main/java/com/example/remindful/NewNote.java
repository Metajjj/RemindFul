package com.example.remindful;

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
import java.util.Calendar;
import java.util.Objects;
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

            if(DataExist){
                Update();
            } else {
                DH.Writequery(
                        DH.InsertBuilder(DH.DBname, new String[]{DH.YMDHMS, DH.TITLE, DH.NOTE},
                                new String[][]{{CalYMDHMS(),
                                        "\"" + ((TextView) findViewById(R.id.NewNoteNoteTitle)).getText().toString() + "\"",
                                        "\"" + ((TextView) findViewById(R.id.NewNoteNoteDetail)).getText().toString() + "\""}}
                        )
                );
            }

            Saved="SAVED";
        }else{
            Toast.makeText(this,"ERR no reminding fn!",Toast.LENGTH_SHORT).show();

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

    private void Update(){
        String title=((TextView)findViewById(R.id.NewNoteNoteTitle)).getText().toString();
        String note=((TextView)findViewById(R.id.NewNoteNoteDetail)).getText().toString();

        String Query = MessageFormat.format("UPDATE `{0}` SET `{1}` = \"{2}\", `{3}` = \"{4}\", `{5}` = {6} WHERE `{7}` = {8} AND `{9}` = {10}",DH.DBname, DH.TITLE, title, DH.NOTE, note, DH.YMDHMS, CalYMDHMS(), DH.ID, G_ID, DH.YMDHMS, G_YMDHMS);

        //new Home().WriteLine(Query+"\n"+getIntent().getExtras().get("i"));
        DH.Writequery(Query);
    }

}


