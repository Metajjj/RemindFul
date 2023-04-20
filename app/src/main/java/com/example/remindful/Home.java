package com.example.remindful;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class Home extends AppCompatActivity {

    public static String Style = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if(Style==null){
            Style = this.getString(R.string.Style);
        }

        if(Style.equals("AltTheme")) { setTheme(R.style.AltTheme); }
        else{ setTheme(R.style.MainTheme); }

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header
        setContentView(R.layout.home);

        new NotificationManagerCust(getApplicationContext());
    }

    private Handler HomeLoadingHandler = new Handler();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK ){
            System.out.println("Back key pressed to leave app!");
            HomeLoadingHandler.removeCallbacksAndMessages(null);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        findViewById(R.id.HomeTitle).setOnClickListener((v)->{
            HomeLoadingHandler.removeCallbacksAndMessages(null);

            ((TextView) v).setText("Alt theme activated!");
            Style = "AltTheme";
            /*this.onStop();
            this.onDestroy();
            onCreate(null);*/
            setTheme(R.style.AltTheme);
            startActivity(new Intent(this,Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        });

        super.onStart();

        HomeLoadingHandler.postDelayed(() -> {

            startActivity(new android.content.Intent(this,Home2.class));

            /*DatabaseHandler DH = new DatabaseHandler(getApplicationContext());
            System.out.println(
                DH.CursorSorter( DH.getReadableDatabase().query(DH.DBname,null,null,null,null,null,"`"+DH.ID+"` DESC") )
            ); DH.close(); //*/

        }, 2000);
    }
}
