package com.example.remindful;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class Home extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header
        setContentView(R.layout.home);

        new NotificationManagerCust(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        ((TextView) findViewById(R.id.HomeTitle)).setText("RemindFul!");
        new Handler().postDelayed(() -> {
            ((TextView) findViewById(R.id.HomeTitle)).setText("Loaded!");
            //try{Thread.sleep(2000);}catch(Exception e){}
            startActivity(new android.content.Intent(this,Home2.class));

            //TODO replace ReadQuery with .query
            /*final DatabaseHandler DH = new DatabaseHandler(getApplicationContext());
            java.util.ArrayList<HashMap<String, String>> Notes = DH.CursorSorter(
                    DH.getReadableDatabase().query(DH.DBname, null, null, null, null, null, null)
            );
            DH.close();

            System.out.println("Notes:\n" + Notes);*/

        }, 2000);
    }
}
