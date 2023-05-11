package com.example.remindful;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Home extends AppCompatActivity {

    public static ArrayList<Integer> Themes = new ArrayList<>(Arrays.asList(R.style.MainTheme));
    //MainTheme is default always present theme

    public static int ThemeNum = 0;

    //Setting custom anims for each activity fired
    @Override
    public void startActivity(Intent i) { super.startActivity(i); overridePendingTransition(R.anim.activity_in,R.anim.activity_out); }
    @Override
    public void startActivity(Intent i, @Nullable Bundle o) { super.startActivity(i, o); overridePendingTransition(R.anim.activity_in,R.anim.activity_out); }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header

        //new Handler().post(()-> {
            // if Themes isnt set up (first launch of app since killed) - setup
            if (Themes.size() <= 1) {
                for (Field f : R.style.class.getDeclaredFields()) {
                    if(f.getName().equals("MainTheme")){ continue; }
                    //Avoids duplicating main theme

                    int i = Integer.MIN_VALUE;
                    try { i = f.getInt(null); } catch (Exception e) { }
                    if (i != Integer.MIN_VALUE) { Themes.add(i); }
                }
            }
        //});

        if ( ! new File(getApplicationContext().getFilesDir(), "F").exists() ){
            //If file doesnt exist.. make it
            try {
                FileWriter FW = new FileWriter(new File(getApplicationContext().getFilesDir(), "F"));
                FW.write( getResources().getResourceEntryName(Themes.get(ThemeNum)) ); FW.flush(); FW.close();
            } catch (Exception e) {
                System.err.println("Err making new file in internal\n "+e);
            }
        }

        try {
            BufferedReader bfr = new BufferedReader( new FileReader( new File(getApplicationContext().getFilesDir(), "F")) );
            String l = bfr.readLine();
            //System.out.println(MessageFormat.format( "Read: {0}  ID: {1} | CurrThemeID: {2}", l, getResources().getIdentifier(l, "style", getPackageName() ) , Themes.get(ThemeNum) ));
            bfr.close();

                                        //Find resID via name
            ThemeNum = Themes.indexOf( getResources().getIdentifier(l, "style", getPackageName() ) );

        }catch (Exception e){ System.err.println("Err w bfr? "+e); }

        setTheme(Themes.get(ThemeNum)); //Have to set theme before layout
        setContentView(R.layout.home);

        //Have to set after layout
        ((TextView)findViewById(R.id.HomeTitle)).setText(
                "RemindFul\n"+getResources().getResourceEntryName(Themes.get(ThemeNum))+" : "+ThemeNum+"/"+ (Themes.size()-1) );

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
    public void onBackPressed() {
        super.onBackPressed();

        System.out.println("Back key pressed!");
        HomeLoadingHandler.removeCallbacksAndMessages(null);

        finishAffinity(); //Closes app - avoid looping to home2
        overridePendingTransition(R.anim.homescreen_in,R.anim.app_out);
        //System.runFinalization();
    }

    @Override
    protected void onStart() {

        findViewById(R.id.HomeTitle).setOnClickListener((v)->{
            HomeLoadingHandler.removeCallbacksAndMessages(null);

            ((TextView) v).setText("Theme switched!");

            ThemeNum = (ThemeNum+1 >= Themes.size()) ? 0 : ++ThemeNum;

            //Record theme
            try {
                FileWriter FW = new FileWriter( new File(getApplicationContext().getFilesDir(), "F") );
                FW.write( getResources().getResourceEntryName(Themes.get(ThemeNum)) ); FW.flush(); FW.close();
            }catch (Exception e){ System.err.println("File Err: "+e); }

            startActivity(new Intent(this,Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            overridePendingTransition(R.anim.theme_in,R.anim.theme_out);

        });

        super.onStart();


        HomeLoadingHandler.postDelayed(() -> {

            startActivity(new android.content.Intent(this,Home2.class));
            overridePendingTransition(R.anim.activity_in,R.anim.activity_out);

            //for(Field f : R.attr.class.getDeclaredFields()){ System.out.println("f: "+f); }

        }, 3000);
        // */
    }
}
