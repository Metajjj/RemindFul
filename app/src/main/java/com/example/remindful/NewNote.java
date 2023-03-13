package com.example.remindful;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
        TextView tv = (TextView) v;
        if( tv.getText() == "SAVE" ){
            //Write to db, move back
            //DH.Writequery("INSERT INTO `"+DH.DBname+"` (a,a,a) VALUES (a,a,a);");
            /*String c = (Calendar.getInstance().get(Calendar.MONTH)+1) +"|"+(Calendar.getInstance().get(Calendar.YEAR));
            new Home().WriteLine(""+c);//*/
        }else{
            Toast.makeText(this,"ERR no reminding fn!",Toast.LENGTH_LONG).show();
        }

    }

}


