package com.example.remindful;

import android.os.Bundle;
import android.os.Handler;

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
        new Handler().postDelayed(() -> {

            startActivity(new android.content.Intent(this,Home2.class));

            /*DatabaseHandler DH = new DatabaseHandler(getApplicationContext());
            System.out.println(
                DH.CursorSorter( DH.getReadableDatabase().query(DH.DBname,null,null,null,null,null,"`"+DH.ID+"` DESC") )
            ); DH.close(); //*/

        }, 2000);
    }
}
