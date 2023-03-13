package com.example.remindful;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class Home2 extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header

        setContentView(R.layout.home2);
    }

    public void TempNoteWipe(View v){
        DatabaseHandler DH = new DatabaseHandler(Home2.this);
        DH.ResetTable();
    }

    public void Menu(View v){
        startActivity(new Intent(this,NewNote.class));
    }

    //Grab crap from DB.. 2x array store vals.. display vals..

    //on Recent txt change.. grab from DB
}
