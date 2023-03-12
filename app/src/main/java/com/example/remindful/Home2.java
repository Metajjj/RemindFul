package com.example.remindful;

import android.os.Bundle;

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

    //Grab crap from DB.. 2x array store vals.. display vals..

    //on Recent txt change.. grab from DB
}
