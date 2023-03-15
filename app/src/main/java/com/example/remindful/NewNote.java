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
    }

    public void Remind(View v){
        CheckBox b = (CheckBox)v;
        ((TextView)findViewById(R.id.NewNoteSave)).setText( (b.isChecked()? "SAVE & REMIND" : "SAVE") );
    }

    public void Save(View v){
        //On click.. background flashes green / toast popup to say saved
        String Saved;

        TextView tv = (TextView) v;
        if( tv.getText() == "SAVE" ){
            //PreparedStatement PreparedStatement PS = PreparedStatement().getConnection().prepareStatement("A");
            //SqlBuilder
            //MONTH = "Month", YEAR = "Year", TITLE = "Title", NOTE = "Note"
            String s = DH.InsertBuilder(DH.DBname,new String[]{DH.MONTH,DH.YEAR,DH.TITLE,DH.NOTE},new String[][]{{String.valueOf(Calendar.getInstance().get(Calendar.MONTH)+1),String.valueOf(Calendar.getInstance().get(Calendar.YEAR)),((TextView)findViewById(R.id.NewNoteNoteTitle)).getText().toString(),((TextView)findViewById(R.id.NewNoteNoteDetail)).getText().toString()}});
            new Home().WriteLine(s);

            //Write to db, move back
            /*String c = String.valueOf(Calendar.getInstance().get(Calendar.MONTH)+1) +"|"+(Calendar.getInstance().get(Calendar.YEAR));
            new Home().WriteLine(""+c);//*/

            Saved="SAVED";
        }else{
            Toast.makeText(this,"ERR no reminding fn!",Toast.LENGTH_LONG).show();

            Saved="SAVED & REMINDED";
        }

        String tv2 = tv.getText().toString();
        tv.setText("✓"+Saved+"✓");
        //32C95E
        tv.setBackgroundColor(Color.rgb(50,201,94));

        new Handler().postDelayed(() -> { ForcedFunc(tv2); },1300);
    }

    private void ForcedFunc(String tv2){
        ((TextView)findViewById(R.id.NewNoteSave)).setText(tv2);
        //d9b453
        findViewById(R.id.NewNoteSave).setBackgroundColor(Color.rgb(217,180,83));
    }

}


