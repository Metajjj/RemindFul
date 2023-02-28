package com.example.remindful;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    private void SetupPermGrabber(){
        //New way of checking permission - has to be created before fragment is
        ARL = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                res -> {
                    if (!res) {
                        //Not granted!
                        Toast.makeText(getApplicationContext(), "Need perm for noti/reminding work!", Toast.LENGTH_LONG).show();
                    }else{
                        //Allowed
                        new NotificationManagerCust(getApplicationContext());
                    }
                }
        );
    }

    public static ActivityResultLauncher<String> ARL;
    public static ArrayList<Integer> Themes = new ArrayList<>(Arrays.asList(R.style.MainTheme));
    //MainTheme is default always present theme

    public static int ThemeNum = 0; private static Toast toast = null;

    protected void SetupThemeList(){
        if (Themes.size() <= 1) {
            for (Field f : R.style.class.getDeclaredFields()) {
                if(f.getName().equals("MainTheme")){ continue; }
                //Avoids duplicating main theme

                int i = Integer.MIN_VALUE;
                try { i = f.getInt(null); } catch (Exception e) { Toast.makeText(getApplicationContext(),"SetupTheme Err:"+e,Toast.LENGTH_LONG).show(); }
                if (i != Integer.MIN_VALUE) { Themes.add(i); }
            }
        }
    }
    @Override
    protected void onDestroy() {
        DatabaseHandler dh = new DatabaseHandler(getApplicationContext());
        dh.close();
        super.onDestroy();
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SetupPermGrabber();

        //GrabPerm for Noti
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ARL.launch(Manifest.permission.POST_NOTIFICATIONS); // Check/grab perm
        }else{
            new NotificationManagerCust(getApplicationContext());
        }

        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header

        //new Handler().post(()-> {
            // if Themes isnt set up (first launch of app since killed) - setup
            SetupThemeList();
        //});

        if ( ! new File(getApplicationContext().getFilesDir(), "F").exists() ){
            //If file doesnt exist.. make it
            try {
                FileWriter FW = new FileWriter(new File(getApplicationContext().getFilesDir(), "F"));
                FW.write( getResources().getResourceEntryName(Themes.get(ThemeNum)) ); FW.flush(); FW.close();
            } catch (Exception e) {
                System.err.println("Err making new file in internal\n "+e);
                Toast.makeText(getApplicationContext(),"MakeFile Err:"+e,Toast.LENGTH_LONG).show();
            }
        }

        try {
            BufferedReader bfr = new BufferedReader( new FileReader( new File(getApplicationContext().getFilesDir(), "F")) );
            String l = bfr.readLine();
            //System.out.println(MessageFormat.format( "Read: {0}  ID: {1} | CurrThemeID: {2}", l, getResources().getIdentifier(l, "style", getPackageName() ) , Themes.get(ThemeNum) ));
            bfr.close();

                                        //Find resID via name
            ThemeNum = Themes.indexOf( getResources().getIdentifier(l, "style", getPackageName() ) );

            //If theme doesnt exist i.e. changed name
            ThemeNum = (Themes.get(ThemeNum) >= 0) ? ThemeNum : 0; //If exist, return it else make it start from 0  -- new theme auto written into file

        }catch (Exception e){ Toast.makeText(getApplicationContext(),"ReadFile Err:"+e,Toast.LENGTH_LONG).show();
            System.err.println("Err w bfr? "+e); }

        setTheme(Themes.get(ThemeNum)); //Have to set theme before layout
        setContentView(R.layout.home);

        //Have to set after layout
        ((TextView)findViewById(R.id.HomeTitle)).setText(
                getString(R.string.Title)+"\n"+getResources().getResourceEntryName(Themes.get(ThemeNum))+" : "+ThemeNum+"/"+ (Themes.size()-1) );
        //index = -1 | size = 13 ??

        if(toast != null){ toast.cancel(); } //cancel if toast exists
        toast = Toast.makeText(this, "!!!RECOMMENDED TO PUT THIS APP's NOTIFICATIONS AS SILENT!!!", Toast.LENGTH_SHORT); //makeText returns toast but .show makes it void
        toast.show();
        //Toast.makeText(this, "!!!RECOMMENDED TO PUT THIS APP's NOTIFICATIONS AS SILENT!!!", Toast.LENGTH_LONG).show();


        //Get attr stuff
        TypedArray ta = this.obtainStyledAttributes(new int[]{R.attr.Interactable}); int ThemeTxtCol = ta.getColor(0,-1); ta.recycle();

        //Change progressbar colour without API err
        /*
        ((ProgressBar)findViewById(R.id.HomeProBar)).getIndeterminateDrawable().setColorFilter(
                ThemeTxtCol, android.graphics.PorterDuff.Mode.SRC_IN
        );*/

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

        ViewGroup vg = findViewById(R.id.HomeLayout);
        vg.setOnTouchListener(this::GestureDetect); vg.setOnClickListener(null);    //Onclick interferes with onTouch
        for(int i=0;i<vg.getChildCount();i++){
            vg.getChildAt(i).setOnTouchListener(this::GestureDetect); vg.getChildAt(i).setOnClickListener(null);
        }
        findViewById(R.id.HomeTitle).setOnClickListener((v)->{ ChangeTheme(); });

        super.onStart();

        HomeLoadingHandler.postDelayed(() -> {

            startActivity(new android.content.Intent(this,Home2.class)
                , ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.activity_in,R.anim.activity_out).toBundle()
            );
            //overridePendingTransition(R.anim.activity_in,R.anim.activity_out);

            //for(Field f : R.attr.class.getDeclaredFields()){ System.out.println("f: "+f); }

        }, 3000);
        // */

        System.out.println("\n\n");

        ARL.launch(Manifest.permission.READ_EXTERNAL_STORAGE);



        System.out.println("\n\n\n");
    }

    private float initY=0;
    private boolean GestureDetect(View v, MotionEvent e){
        //System.out.println("GestureDetect");
        float CurrY = e.getRawY();

        //System.out.println("InitY: "+initY+" CurrY: "+CurrY);

        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                initY = CurrY; break;
            case MotionEvent.ACTION_MOVE:
                //0 = top  -- Down : +1 | Up : -1
                //System.out.println("CurrY: "+CurrY);

                break;
            case (MotionEvent.ACTION_UP):
                //System.out.println("Mup");
                //System.out.println("InitY: "+initY+" CurrY: "+CurrY);

                //System.out.println("InitY: "+initY+" CurrY: "+CurrY);
                if (Math.abs(initY-CurrY) >= 300){
                    //Change theme

                    ViewGroup vg = findViewById(R.id.HomeLayout); vg.setOnTouchListener(null);
                    for(int i=0;i<vg.getChildCount();i++){
                        vg.getChildAt(i).setOnTouchListener(null);
                    }

                    if (initY-CurrY < 0){
                        //System.out.println("<0");
                        ChangeTheme();
                    }
                    else{
                        //System.out.println(">0");
                        ChangeTheme(false);
                    }

                    vg.setOnTouchListener(this::GestureDetect);
                    for(int i=0;i<vg.getChildCount();i++){
                        vg.getChildAt(i).setOnTouchListener(this::GestureDetect);
                    }
                }

                break;
            default:break;
        }

        return super.onTouchEvent(e);
    }

    private void ChangeTheme(){ ChangeTheme(true); }
    private void ChangeTheme(boolean incr){
        HomeLoadingHandler.removeCallbacksAndMessages(null);

        ((TextView) findViewById(R.id.HomeTitle)).setText("Theme switched!");

        if(incr) { ThemeNum = (ThemeNum + 1 >= Themes.size()) ? 0 : ++ThemeNum; }
        else{ ThemeNum = (ThemeNum - 1 < 0 ) ? Themes.size()-1 : --ThemeNum; }

        //Record theme
        try {
            FileWriter FW = new FileWriter( new File(getApplicationContext().getFilesDir(), "F") );
            FW.write( getResources().getResourceEntryName(Themes.get(ThemeNum)) ); FW.flush(); FW.close();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"RecordTheme Err:"+e,Toast.LENGTH_LONG).show();
            System.err.println("File Err: "+e); }

        startActivity(new Intent(this,Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            , ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.theme_in,R.anim.theme_out).toBundle()
        );
    }
}
