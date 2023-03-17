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

import java.util.Calendar;
import java.util.Objects;

public class NewNote extends AppCompatActivity {
    private final DatabaseHandler DH = new DatabaseHandler(NewNote.this);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header
        setContentView(R.layout.newnote);

        Remind(findViewById(R.id.NewNoteRemindBox));

        //check for extra bundle intent crap and load it up === INSTERT => UPDATE/ALTER
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
            String s = DH.InsertBuilder(DH.DBname,new String[]{DH.YMDHMS,DH.TITLE,DH.NOTE},
                    new String[][]{{CalYMDHMS(),
                       "\""+((TextView)findViewById(R.id.NewNoteNoteTitle)).getText().toString()+"\"",
                       "\""+((TextView)findViewById(R.id.NewNoteNoteDetail)).getText().toString()+"\""}});
            new Home().WriteLine(s);

            DH.Writequery(s);

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

}


