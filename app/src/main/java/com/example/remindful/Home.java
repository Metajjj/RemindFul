package com.example.remindful;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

public class Home extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header

        setContentView(R.layout.home);

        ColScheme( findViewById(R.id.HomeLayout) );

        new Handler().postDelayed(()->{
            ((TextView)findViewById(R.id.HomeTitle)).setText("Loaded!");
            //try{Thread.sleep(2000);}catch(Exception e){}
            startActivity(new Intent(this,Home2.class));

        },3000);

    }

    protected void ColScheme(ConstraintLayout LL) {
        LL.setBackgroundColor(Color.parseColor("#AB9866"));
        for (int i = 0; i < LL.getChildCount(); i++) {
            if ((LL.getChildAt(i)) instanceof Button) {
                (LL.getChildAt(i)).setBackgroundColor(Color.parseColor("#D9B453"));
                continue;
            }
            try { ((TextView) LL.getChildAt(i)).setTextColor(Color.parseColor("#D2B771"));
                }
            catch (Exception e) { }
        }
    }
}
