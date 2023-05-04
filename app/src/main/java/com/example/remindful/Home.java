package com.example.remindful;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Home extends AppCompatActivity {

    public static ArrayList<Integer> Themes = new ArrayList<>(Arrays.asList(R.style.MainTheme));
    //MainTheme is default always present theme

    public static int ThemeNum = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        //Move to alt thread??
        new Handler().post(()-> {
            if (Themes.size() <= 1) {
                for (Field f : R.style.class.getDeclaredFields()) {
                    if(f.getName().equals("MainTheme")){ continue; }
                    //Avoids duplicating main theme

                    int i = Integer.MIN_VALUE;
                    try { i = f.getInt(null); } catch (Exception e) { }
                    if (i != Integer.MIN_VALUE) { Themes.add(i); }
                }
            }
        });

        setTheme(Themes.get(ThemeNum));

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header
        setContentView(R.layout.home);

        Toast.makeText(this, "!!!RECOMMENDED TO PUT THIS APP's NOTIFICATIONS AS SILENT!!!", Toast.LENGTH_LONG).show();

        new NotificationManagerCust(getApplicationContext());


        //Get attr stuff
        TypedArray ta = this.obtainStyledAttributes(new int[]{R.attr.Interactable}); int ThemeTxtCol = ta.getColor(0,-1); ta.recycle();

        //Change progressbar colour without API err
        ((ProgressBar)findViewById(R.id.HomeProBar)).getIndeterminateDrawable().setColorFilter(
                ThemeTxtCol, android.graphics.PorterDuff.Mode.SRC_IN
        );
    }

    private Handler HomeLoadingHandler = new Handler();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK ){
            System.out.println("Back key pressed!");
            HomeLoadingHandler.removeCallbacksAndMessages(null);

            finishAffinity(); //Closes app - avoid looping to home2
            //System.runFinalization();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {

        findViewById(R.id.HomeTitle).setOnClickListener((v)->{
            HomeLoadingHandler.removeCallbacksAndMessages(null);

            ((TextView) v).setText("Theme switched!");

            ThemeNum = (ThemeNum+1 >= Themes.size()) ? 0 : ++ThemeNum;

            startActivity(new Intent(this,Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        });

        super.onStart();

        HomeLoadingHandler.postDelayed(() -> {

            startActivity(new android.content.Intent(this,Home2.class));

            //for(Field f : R.attr.class.getDeclaredFields()){ System.out.println("f: "+f); }

        }, 3000);
    }
}
