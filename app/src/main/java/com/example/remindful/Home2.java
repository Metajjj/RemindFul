package com.example.remindful;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Home2 extends AppCompatActivity {
    private final DatabaseHandler DH = new DatabaseHandler(Home2.this);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header

        setContentView(R.layout.home2);

        new Handler().post(()->{
            TempLoad("ORDER BY `"+DH.YMDHMS+"` DESC, `"+DH.TITLE+"` ASC");
        });
    }

    public void Switchy(View v){
        TextView tv = (TextView) v;
        //new Home().WriteLine(tv.getText()+"");
        switch (tv.getText()+""){
            case "Recent":
                tv.setText("Alphabetical");
                new Handler().post(()-> { TempLoad("ORDER BY `"+DH.TITLE+"` ASC,`"+DH.YMDHMS+"` DESC"); });
                break;
            case "Alphabetical":
                tv.setText("Recent");
                new Handler().post(()-> { TempLoad("ORDER BY `"+DH.YMDHMS+"` DESC, `"+DH.TITLE+"` ASC"); });
                break;
            default:
                Toast.makeText(this, "ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
        }
    }

    public void TempNoteWipe(View v){
        DatabaseHandler DH = new DatabaseHandler(Home2.this);
        DH.ResetTable();
        Toast.makeText(this,"WIPED notes",Toast.LENGTH_SHORT).show();

        Switchy(findViewById(R.id.home2ViewStyle));
    }

    private void TempLoad(String sort){
        ((TableLayout)findViewById(R.id.NewNoteTable)).removeAllViews();

        String catc = DH.Readquery("SELECT * FROM `"+DH.DBname+"` "+sort);
        if(catc.equals("")){ NotesMissing(); }
        else{ DisplayNotes(catc); }
    }

    public void Menu(View v){

        new AlertDialog.Builder(Home2.this, 0).setTitle("MENU").setItems(new String[]{"Add a new note","Del a note","Wipe all notes!"},
                ((dialogInterface, i) -> {
                    //Toast.makeText(Home2.this,""+i,Toast.LENGTH_SHORT).show();
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

    public void OpenNote(View v){
        String ID = v.getTag()+"";
        if (ID.equals("")){ startActivity(new Intent(this,NewNote.class)); }
        else{
            //Compare against db n load intent via db // check here or newnote?
        }
        //Tag is available... load newnote with intent and bundle extra
    }

    private void NotesMissing(){
        //Create the default and display..
        ArrayList<TextView[]> TxtVwHldr = new ArrayList<>();
        TxtVwHldr.add(SetupCols("Note1\nLine2..\nEnd line..","New note...",""));
        TxtVwHldr.add(SetupCols("Note2 example","BB",""));
        TxtVwHldr.add(SetupCols("Note3 exmple","CC",""));

        //WORKS
        ArrayList<TextView> Notes=new ArrayList<>(),Titles=new ArrayList<>();
        for(TextView[] Tv : TxtVwHldr){ for(int i=0;i< Tv.length;i++){
                switch (i%2){
                    case 0: Notes.add(Tv[i]); break;
                    default: Titles.add(Tv[i]); break;
                }
        } }

        for(int i=0;i<Notes.size();i+=3){
            AddTblRow(new TextView[]{ Notes.get(i),Notes.get(i+1),Notes.get(i+2) },
                    new TextView[]{ Titles.get(i),Titles.get(i+1),Titles.get(i+2) }
            ); //1,2,3 notes, titles
        }
    }

    private void AddTblRow(TextView[] Notes3, TextView[] Titles3){
        TableLayout MainLayout = findViewById(R.id.NewNoteTable);
        TableRow NoteRow= new TableRow(Home2.this), NoteTitle = new TableRow(Home2.this);
        //Add table row.. put in notes.. etc for title..
        for(int i=0;i< Notes3.length;i++){
            NoteRow.addView(Notes3[i]); NoteTitle.addView(Titles3[i]);
        }

        MainLayout.addView(NoteRow,new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        MainLayout.addView(NoteTitle,new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private TextView[] SetupCols(String note, String title, String TagID){
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
        TvTitle.setTypeface(null, Typeface.BOLD);

        TvTitle.setTextColor(Color.parseColor("#59453F"));
        TvTitle.setLayoutParams(TempParam);



        TvNote.setText(note); TvTitle.setText(title);
        TvNote.setTag(""+TagID); TvTitle.setTag(""+TagID);
        TvNote.setOnClickListener(this::OpenNote); TvTitle.setOnClickListener(this::OpenNote);
        return new TextView[]{TvNote,TvTitle};
    }

    private void DisplayNotes(String notes){
        ArrayList<TextView[]> TVHldr = new ArrayList<>(); ArrayList<TextView> Notes=new ArrayList<>(),Titles=new ArrayList<>(); Matcher m1,m2,m3;
        //new Home().WriteLine(notes); //xx:xx|yy:yy\nx2:x2|y2:y2\n
        //new Home().WriteLine(notes);
        for(String s : notes.split("\\n"))
        {
            //Split s into Title,Note,YMHSD
            m1= Pattern.compile("Note:[\\s\\w\\d]+\\|").matcher(s);
            m2= Pattern.compile("Title:[\\s\\w\\d]+\\|").matcher(s);
            m3= Pattern.compile("YMDHMS:[\\s\\w\\d]+\\|").matcher(s);
            if (m1.find() && m2.find() && m3.find()) {
                TVHldr.add(SetupCols(
                        s.substring(m1.start() + "Note:".length(), m1.end() - 1),
                        s.substring(m2.start() + "Title:".length(), m2.end() - 1),
                        s.substring(m3.start() + "YMDHMS:".length(), m3.end() - 1))
                );
            } else{
                Toast.makeText(Home2.this,"Error occured when accessing db, possible corruption!",Toast.LENGTH_LONG).show();
            }
        }

        //Splits Cols into their rows/arrays
        for(TextView[] Tv : TVHldr){ for(int i=0;i< Tv.length;i++){
            switch (i%2){
                case 0: Notes.add(Tv[i]); break;
                default: Titles.add(Tv[i]); break;
            }
        } }

        //FIX
        for(int i=0;i<Notes.size();i+=3){
            ArrayList<TextView> tv1 = new ArrayList<>(), tv2 = new ArrayList<>();
            for(int j=i;j<Notes.indexOf( Notes.get(i) )+3;j++){
                try{ tv1.add(Notes.get(j)); tv2.add(Titles.get(j)); } catch (Exception e){}
            }
            AddTblRow( tv1.toArray(new TextView[tv1.size()]), tv2.toArray(new TextView[tv1.size()]) );
            //1,2,3 notes, titles 2?1?3?
            //new Home().WriteLine("CheckRows\n"+tv1.get(0).getText()+"|"+tv1.get(1).getText()+"\n"+tv2.get(0).getText()+"|"+tv2.get(1).getText());
        }
    }

    private float DPtoPixel(int DP){
        //PixeltoDP
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,DP,getResources().getDisplayMetrics());
    }

    //Grab crap from DB.. 2x array store vals.. display vals..

    //on Recent txt change.. grab from DB
}
