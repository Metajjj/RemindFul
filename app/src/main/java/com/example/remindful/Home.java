package com.example.remindful;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.HashMap;
import java.util.Objects;

public class Home extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header
        setContentView(R.layout.home);

        ColScheme(findViewById(R.id.HomeLayout));
    }

    @Override
    protected void onStart() {
        super.onStart();
        ((TextView)findViewById(R.id.HomeTitle)).setText("RemindFul!");
        new Handler().postDelayed(()->{
            ((TextView)findViewById(R.id.HomeTitle)).setText("Loaded!");
            //try{Thread.sleep(2000);}catch(Exception e){}
            //startActivity(new Intent(this,Home2.class));

            //TODO replace ReadQuery with .query
            final DatabaseHandler DH = new DatabaseHandler(getApplicationContext());
            // Select Cols1,2,3 from Table Where Col1=? OR Col2=?; ==  .query (Table; Col1,Col2,Col3; Col1=? OR Col2=?; Arg1,Arg2; groupBy, Having, OrderBy

            java.util.ArrayList<HashMap<String,String>> Notes = DH.CursorSorter(
                    DH.getReadableDatabase().query(DH.DBname, null,null,null,null,null,null)
            );
            DH.close();

            System.out.println( "Notes:\n"+Notes );

        },2000);
    }

    public void WriteLine(String s){
        System.out.println("\n======\n"+s+"\n======\n");
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
