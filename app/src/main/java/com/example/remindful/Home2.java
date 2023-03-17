package com.example.remindful;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class Home2 extends AppCompatActivity {
    private final DatabaseHandler DH = new DatabaseHandler(Home2.this);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header

        setContentView(R.layout.home2);

        //TempLoad(new View(this));
    }

    public void Switchy(View v){
        TextView tv = (TextView) v;
        new Home().WriteLine(tv.getText()+"");
        switch (tv.getText()+""){
            case "Recent":
                tv.setText("Alphabetical"); break;
            case "Alphabetical":
                tv.setText("Recent"); break;
            default:
                Toast.makeText(this, "ERROR OCCURED!", Toast.LENGTH_SHORT).show();
        }
    }

    public void TempNoteWipe(View v){
        DatabaseHandler DH = new DatabaseHandler(Home2.this);
        DH.ResetTable();
        Toast.makeText(this,"WIPED DB",Toast.LENGTH_SHORT).show();
    }

    public void TempLoad(View v){
        String catc = DH.Readquery("SELECT * FROM `"+DH.DBname+"` ORDER BY `"+DH.TITLE+"` ASC;");
        new Home().WriteLine(catc);
        if(catc.equals("")){
            NotesMissing();
        } else{
            DisplayNotes(catc);
        }
    }

    public void Menu(View v){

        new AlertDialog.Builder(Home2.this, 0).setTitle("MENU").setItems(new String[]{"Add a new note","Del a note","Wipe all notes!"},
                ((dialogInterface, i) -> {
                    Toast.makeText(Home2.this,""+i,Toast.LENGTH_SHORT).show();
                    switch(i){
                        case 0:
                            startActivity(new Intent(this,NewNote.class)); break;
                        case 1: //Red border, onclick = del - forewarn, is perm
                            break;
                        case 2:
                        TempNoteWipe(new View(this)); break;
                    }
                })
            ).setCancelable(true).create().show();
    }

    private void NotesMissing(){
        //Create the default and display..
        TableLayout MainLayout = findViewById(R.id.NewNoteTable);
        //forloop(Cols1,2,3)
        // for loop (Rows1,2)
    }

    private float DPtoPixel(int DP){
        //PixeltoDP
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,DP,getResources().getDisplayMetrics());
    }

    private void AddTblRow(){
        TextView TvNote = new TextView(this), TvTitle = new TextView(this);
    }

    private TextView[] SetupCols(String note, String title){
        TextView TvNote = new TextView(Home2.this), TvTitle = new TextView(Home2.this);
        TableRow.LayoutParams TempParam;

        //TvNote
        TempParam = new TableRow.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);
        TempParam.setMargins((int)Math.ceil(DPtoPixel(5)),(int)Math.ceil(DPtoPixel(5)),(int)Math.ceil(DPtoPixel(5)),(int)Math.ceil(DPtoPixel(5))); TvNote.setPadding((int)Math.ceil(DPtoPixel(10)),(int)Math.ceil(DPtoPixel(10)),(int)Math.ceil(DPtoPixel(10)),(int)Math.ceil(DPtoPixel(10)));

        TvNote.setEllipsize(TextUtils.TruncateAt.END);
        TvNote.setMaxLines(3);
        TvNote.setTypeface(null, Typeface.BOLD);

        TvNote.setTextColor(Color.parseColor("#59453F"));
        TvNote.setBackgroundResource(R.drawable.roundbordernote);
        TvNote.setLayoutParams(TempParam);


        //TvTitle
        TempParam = new TableRow.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);
        TvTitle.setGravity(Gravity.CENTER); TvTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TvTitle.setMaxLines(1);
        TvTitle.setEllipsize(TextUtils.TruncateAt.END);
        TvNote.setTypeface(null, Typeface.BOLD);

        TvNote.setTextColor(Color.parseColor("#59453F"));
        TvTitle.setLayoutParams(TempParam);



        TvNote.setText(note); TvTitle.setText(title);
        TvNote.setTag("Note5");
        return new TextView[]{TvNote,TvTitle};
    }

    private void DisplayNotes(String notes){
        notes=""; //regex
        //R: Img = 3 multiline
        //R: Title = 1 line
        // Controls height size of row easy as its based on txt size, uniformally
    }

    //Grab crap from DB.. 2x array store vals.. display vals..

    //on Recent txt change.. grab from DB
}
