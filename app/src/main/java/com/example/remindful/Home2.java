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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Home2 extends AppCompatActivity {
    //CTRL SHIFT +   opens all brackets
    //CTRL SHIFT -   closes all brackets
    //TODO - fix tempload out of sync with switchy
    private final DatabaseHandler DH = new DatabaseHandler(Home2.this);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header

        setContentView(R.layout.home2);

        findViewById(R.id.home2ViewStyle).performClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().post(()->{
            findViewById(R.id.home2ViewStyle).performClick();
        });
    }

    public void Switchy(View v){
        TextView tv = (TextView) v;
        //new Home().WriteLine(tv.getText()+"");
        switch (tv.getText()+""){
            case "Recent":
                switchy();
                tv.setText("Alphabetical");
                break;
            case "Alphabetical":
                switchy();
                tv.setText("Recent");
                break;
            default:
                Toast.makeText(this, "ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
        }
    }

    private void switchy(){
        TextView tv = findViewById(R.id.home2ViewStyle);
        switch (tv.getText()+""){
            case "Recent":
                new Handler().post(()-> { TempLoad("ORDER BY `"+DH.TITLE+"` ASC,`"+DH.YMDHMS+"` DESC"); });
                break;
            case "Alphabetical":
                new Handler().post(()-> { TempLoad("ORDER BY `"+DH.YMDHMS+"` DESC, `"+DH.TITLE+"` ASC"); });
                break;
            default:
                Toast.makeText(this, "ERROR OCCURRED!", Toast.LENGTH_SHORT).show();
        }
    }

    private void TempNoteWipe(View v){
        DatabaseHandler DH = new DatabaseHandler(Home2.this);
        DH.ResetTable();
        Toast.makeText(this,"WIPED notes",Toast.LENGTH_SHORT).show();

        switchy();
    }

    private void TempLoad(String sort){
        ((TableLayout)findViewById(R.id.NewNoteTable)).removeAllViews();

        ArrayList catc = DH.Readquery("SELECT * FROM `"+DH.DBname+"` "+sort+";");
        //System.out.println("Catc: "+catc[0].equals(""));

        if(catc.size()==0){ NotesMissing(); }
        else{
            try {
                DisplayNotes( catc );
            } catch (Exception e){
                System.out.println("ERR: "+e);
                Toast.makeText(this, "Serious error occured!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Menu(View v){
        new AlertDialog.Builder(Home2.this, 0).setTitle("MENU").setItems(new String[]{"Add a new note","Del note(s)","Wipe all notes!\n  (permanent & instant, no confirmation!)"},
                ((dialogInterface, i) -> {
                    //Toast.makeText(Home2.this,""+i,Toast.LENGTH_SHORT).show();
                    switch(i){
                        case 0:
                            startActivity(new Intent(this,NewNote.class)); break;
                        case 1:
                            SetupDelArea();
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
            //String o = DH.Readquery("SELECT * FROM `"+DH.DBname+"` WHERE `"+DH.ID+"` = "+v.getTag().toString().split("-")[1]+" AND "+DH.YMDHMS+" = "+v.getTag().toString().split("-")[0]+";");

            ArrayList<HashMap> o = DH.Readquery(MessageFormat.format("SELECT * FROM `{0}` WHERE {1} = {2} AND {3} = {4};",DH.DBname,DH.ID,v.getTag().toString().split("-")[1],DH.YMDHMS,v.getTag().toString().split("-")[0]));
            //new String[0];

            startActivity(new Intent(Home2.this,NewNote.class).putExtra("i",(HashMap<String,String>) o.get(0)));
        }
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

    private void DisplayNotes(ArrayList<HashMap> notes){
        ArrayList<TextView[]> TVHldr = new ArrayList<>(); ArrayList<TextView> Notes=new ArrayList<>(),Titles=new ArrayList<>();

        for( HashMap s : notes ) {
            System.out.println("==\n"+s+"\n==");

            TVHldr.add(SetupCols(
                    s.get(DH.NOTE)+"",
                    s.get(DH.TITLE)+"",
                    s.get(DH.YMDHMS)+"-"+s.get(DH.ID)
            ));

        }

        //Splits Cols into their rows/arrays
        for(TextView[] Tv : TVHldr){ for(int i=0;i< Tv.length;i++){
            switch (i%2){
                case 0: Notes.add(Tv[i]); break;
                default: Titles.add(Tv[i]); break;
            }
        } }

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

    public float DPtoPixel(int DP){
        //PixeltoDP
        ////get resources from frag - fix
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,DP,getApplicationContext().getResources().getDisplayMetrics());
    }

    private void SetupDelArea(){
        Toast.makeText(Home2.this,"Del time!",Toast.LENGTH_LONG).show();

        getSupportFragmentManager().beginTransaction().replace(R.id.home2FragHolder,DeleteFragment.class,null).commit();

        findViewById(R.id.home2FragHolder).bringToFront();

        /*View DL = getLayoutInflater().inflate(R.layout.delete_fragment,null);
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setView(DL).setTitle("Choose the note(s) to delete!");
        b.create().show(); b.setCancelable(true);*/

        //Red border, onclick = del - forewarn, is perm
        //set background res.. del del check box del... ??
        //On click checkbox tick.. entire note/col turns red background

    }
}
