package com.example.remindful;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class Home extends AppCompatActivity {

    public static int[] Themes = null;
    public static int ThemeNum = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if(Themes==null){

            Themes= new int[]{R.style.MainTheme, R.style.PinkTheme, R.style.BlacknWhiteTheme};

        }

        setTheme((Integer) Themes[ThemeNum]);



        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header
        setContentView(R.layout.home);

        Toast.makeText(this, "!!!RECOMMENDED TO PUT THIS APP's NOTIFICATIONS AS SILENT!!!", Toast.LENGTH_LONG).show();

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

            ((TextView) v).setText("Theme switched!");

            ThemeNum = (ThemeNum+1 >= Themes.length) ? 0 : ++ThemeNum;

            startActivity(new Intent(this,Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        });

        super.onStart();

        HomeLoadingHandler.postDelayed(() -> {

            startActivity(new android.content.Intent(this,Home2.class));

            /*DatabaseHandler DH = new DatabaseHandler(getApplicationContext());
            System.out.println(
                DH.CursorSorter( DH.getReadableDatabase().query(DH.DBname,null,null,null,null,null,"`"+DH.ID+"` DESC") )
            ); DH.close(); //*/

        }, 3000);
    }
}
