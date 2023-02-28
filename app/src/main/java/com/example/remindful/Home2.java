package com.example.remindful;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Home2 extends AppCompatActivity {
    //CTRL SHIFT +   opens all brackets
    //CTRL SHIFT -   closes all brackets

    private ActivityResultLauncher<String> ARL;
    private void SetupPermGrab(){
        ARL = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> { }
        );
    }

    @Override
    protected void onStop() {
        DatabaseHandler dh = new DatabaseHandler(getApplicationContext());
        dh.close();
        super.onStop();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(Home.Themes.get(Home.ThemeNum));

        SetupPermGrab();

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header

        setContentView(R.layout.home2);

        //Add touch listeners to all views for gestures
        new Thread(()->{

        ViewGroup BG = findViewById(R.id.home2Bg);
        BG.setOnTouchListener( this::CustTouchEvent );
        for(int i=0;i<BG.getChildCount();i++ ){
            BG.getChildAt(i).setOnTouchListener( this::CustTouchEvent );
        }

        ViewGroup DV = findViewById(R.id.home2DvBg);
            DV.setOnClickListener(null); DV.setOnTouchListener(this::CustTouchEvent);
        for(int i=0;i<DV.getChildCount();i++){ DV.getChildAt(i).setOnClickListener(null);
            //DV.getChildAt(i).setOnTouchListener((v,e)->{ return super.onTouchEvent(e); });
        }

        runOnUiThread(()-> {
            DV.setTranslationX(getResources().getDisplayMetrics().widthPixels * -1); //Moves left and hides view
            DV.bringToFront(); //((ViewGroup)((ViewGroup)DV.getChildAt(1)).getChildAt(0)).removeAllViews();
        });

        }).start();

        /*
        ARL.launch(Manifest.permission.KILL_BACKGROUND_PROCESSES);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.KILL_BACKGROUND_PROCESSES) == PackageManager.PERMISSION_DENIED){
            Snackbar s = Snackbar.make(BG,"OPTIONAL: enabling perm allows app to destroy itself via main notification",Snackbar.LENGTH_LONG);
            TypedArray ta = this.obtainStyledAttributes(new int[]{R.attr.Text,R.attr.FragBackground});
            s.setTextColor(ta.getColor(0,-1)); s.setBackgroundTint(ta.getColor(1,-1));
            s.show();
        }*/
    }

    /*
    //Setting custom anims for each activity fired
    @Override
    public void startActivity(Intent i) { super.startActivity(i); overridePendingTransition(R.anim.activity_in,R.anim.activity_out); }
    @Override
    public void startActivity(Intent i, @Nullable Bundle o) { super.startActivity(i, o); overridePendingTransition(R.anim.activity_in,R.anim.activity_out); }//*/

    @Override
    protected void onStart() {
        try {
            BufferedReader bfr = new BufferedReader( new FileReader( new File(getApplicationContext().getFilesDir(), "F")) );
            String l = bfr.readLine(); bfr.close();

            //currtheme is manifest theme ?? how grab activity theme..
            if (getResources().getIdentifier(l,"style",getPackageName()) != Home.Themes.get(Home.ThemeNum)){

                //System.out.println(getResources().getIdentifier(l,"style",getPackageName()) +" | "+ Home.Themes.get(Home.ThemeNum));

                //* TODO big err constant reload if theme is non existent

                Toast.makeText(getApplicationContext(),"Theme lost, reloading!", Toast.LENGTH_SHORT).show();

                recreate(); //Restart activity if theme not same


                 //*/
            }
            //Toast.makeText(getApplicationContext(),"CurrTheme: " + getResources().getIdentifier(l,"style",getPackageName()) + "\nFileTheme: " + Home.Themes.get(Home.ThemeNum),Toast.LENGTH_LONG).show();
        }catch (Exception e){ System.err.println("ERR: "+e);}

        super.onStart();

        findViewById(R.id.home2Menu).setOnClickListener(v-> Menu());
        findViewById(R.id.home2ViewStyle).setOnClickListener(this::ChangeOrder);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().post(this::NewDisplayOrder);
    }

    private void ChangeOrder(View v){
        String Al="Alphabetical", Re="Recent";
        TextView tv = (TextView) v;
        if(tv.getText().toString().equals(Re)){
            tv.setText(Al);
        }else if(tv.getText().toString().equals(Al)) {
            tv.setText(Re);
        } else{
            Toast.makeText(this, "Error occurred when switching order!", Toast.LENGTH_SHORT).show();
            return;
        }
        NewDisplayOrder();
    }

    private void NewDisplayOrder(){
        DatabaseHandler DH = new DatabaseHandler(this);

        TextView tv = findViewById(R.id.home2ViewStyle);
        switch (tv.getText()+""){
            case "Recent":
                new Handler().post(()-> { TempLoad("`"+DH.YMDHMS+"` DESC, `"+DH.TITLE+"` ASC"); });
                break;
            case "Alphabetical":
                new Handler().post(()-> { TempLoad("`"+DH.TITLE+"` ASC,`"+DH.YMDHMS+"` DESC"); });
                break;
            default:
                Toast.makeText(this, "Err in NewDisplayOrder!", Toast.LENGTH_SHORT).show();
        }

        DH.close();
    }

    private void TempLoad(String sort){
        DatabaseHandler DH = new DatabaseHandler(getApplicationContext());

        ((TableLayout)findViewById(R.id.home2Table)).removeAllViews();

        ArrayList<HashMap<String,String>> catc = DH.CursorSorter( DH.getReadableDatabase().query(DH.DBname,null,null,null,null,null,sort) );
        //System.out.println("Catc: "+catc[0].equals(""));

        //Empty out DetailedView each reload of notes
        ((TableLayout)findViewById(R.id.home2DvTable)).removeAllViews();
        /*new Handler().post(()->{
            TableLayout tl = findViewById(R.id.home2DvBg).findViewById(R.id.home2DvTable);
            for(int i=2;i<tl.getChildCount();i++){
                tl.removeViewAt(i);
            }
            for(int i=2;i<tl.getChildCount();i++){
                System.out.println( ((TextView)((TableRow) tl.getChildAt(i)).getChildAt(0)).getText()+"" );
            }
        });*/

        if(catc.size()==0){ NotesMissing(); }
        else{
            try {
                DisplayNotes( catc );
            } catch (Exception e){
                System.out.println("ERR: "+e);
                Toast.makeText(this, "Serious error occured! If you have created a lot of notes, try wiping all notes to reset DB", Toast.LENGTH_SHORT).show();
            }
        }
        DH.close();
    }

    private void Menu(){
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.frag_in, R.anim.frag_out)
                .replace(R.id.home2FragHolder, MenuFragment.class,null).commit();
        findViewById(R.id.home2FragHolder).bringToFront();
    }

    private void OpenNote(View v){
        DatabaseHandler DH = new DatabaseHandler(getApplicationContext());

        String ID = v.getTag()+"";
        if (ID.equals("")){ startActivity(
            new Intent(this,NewNote.class)
            , ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.activity_in,R.anim.activity_out).toBundle()
        ); }
        else{
            //String o = DH.Readquery("SELECT * FROM `"+DH.DBname+"` WHERE `"+DH.ID+"` = "+v.getTag().toString().split("-")[1]+" AND "+DH.YMDHMS+" = "+v.getTag().toString().split("-")[0]+";");

            ArrayList<HashMap<String,String>> o = DH.CursorSorter(DH.getReadableDatabase().query(DH.DBname,null,DH.ID+" = ? AND "+DH.YMDHMS+" = ?",new String[]{v.getTag().toString().split("-")[1],v.getTag().toString().split("-")[0]},null,null,null) );

            startActivity(
                new Intent(Home2.this,NewNote.class).putExtra("i", o.get(0))
                , ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.activity_in,R.anim.activity_out).toBundle()
            );
        }

        DH.close();
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

        new Handler().post(()-> {
            for (int i = 0; i < Notes.size(); i += 3) {
                AddTblRow(new TextView[]{Notes.get(i), Notes.get(i + 1), Notes.get(i + 2)},
                        new TextView[]{Titles.get(i), Titles.get(i + 1), Titles.get(i + 2)}
                ); //1,2,3 notes, titles
            }
        });
    }

    private void AddTblRow(TextView[] Notes3, TextView[] Titles3){
        TableLayout MainLayout = findViewById(R.id.home2Table);
        TableRow NoteRow= new TableRow(Home2.this), NoteTitle = new TableRow(Home2.this);
        //Add table row.. put in notes.. etc for title..
        for(int i=0;i< Notes3.length;i++){
            NoteRow.addView(Notes3[i]); NoteTitle.addView(Titles3[i]);
        }

        runOnUiThread(()-> {
            MainLayout.addView(NoteRow, new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            MainLayout.addView(NoteTitle, new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        });
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

        //WORKS gets attr val  -- calls recycle for memory cache release so no single line
        TypedArray ta = this.obtainStyledAttributes(new int[]{R.attr.Text}); int ThemeTxtCol = ta.getColor(0,-1); ta.recycle();
        //drw == -1 if no col for attr

        TvNote.setTextColor(ThemeTxtCol);

        TvNote.setBackgroundResource(R.drawable.roundbordernote);
        TvNote.setLayoutParams(TempParam);

        //TvTitle
        TempParam = new TableRow.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);
        TvTitle.setGravity(Gravity.CENTER); TvTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TvTitle.setSingleLine(true); //TvTitle.setMaxLines(1); Spams max line height err
        TvTitle.setEllipsize(TextUtils.TruncateAt.END);
        TvTitle.setTypeface(null, Typeface.BOLD);

        TvTitle.setTextColor(ThemeTxtCol);
        TvTitle.setLayoutParams(TempParam);

        TvNote.setText(note); TvTitle.setText(title);
        TvNote.setTag(""+TagID); TvTitle.setTag(""+TagID);
        TvNote.setOnClickListener(this::OpenNote); TvTitle.setOnClickListener(this::OpenNote);
        TvNote.setOnLongClickListener(null); TvTitle.setOnLongClickListener(null);
        TvNote.setOnTouchListener(this::CustTouchEvent); TvTitle.setOnTouchListener(this::CustTouchEvent);
        return new TextView[]{TvNote,TvTitle};
    }

    private void DisplayNotes(ArrayList<HashMap<String,String>> notes){
        DatabaseHandler DH = new DatabaseHandler(getApplicationContext());

        ArrayList<TextView[]> TVHldr = new ArrayList<>(); ArrayList<TextView> Notes=new ArrayList<>(),Titles=new ArrayList<>();

        new Thread(()-> {

            for (HashMap<String, String> s : notes) {
                //System.out.println(s);
                runOnUiThread( ()-> {
                    ((TextView) findViewById(R.id.home2Title)).setText("Loading..." + (notes.indexOf(s) + 1) + "/" + notes.size());
                });

                TVHldr.add(SetupCols(
                        s.get(DH.NOTE) + "",
                        s.get(DH.TITLE) + "",
                        s.get(DH.YMDHMS) + "-" + s.get(DH.ID)
                ));

                //DetailedViewSetup(s.get(DH.TITLE)+"",s.get(DH.YMDHMS)+"-"+s.get(DH.ID));
            }

            //Splits Cols into their rows/arrays
            for (TextView[] Tv : TVHldr) {
                for (int i = 0; i < Tv.length; i++) {
                    switch (i % 2) {
                        case 0:
                            Notes.add(Tv[i]);
                            break;
                        default:
                            Titles.add(Tv[i]);
                            break;
                    }
                }
            }

            for (int i = 0; i < Notes.size(); i += 3) {
                ArrayList<TextView> tv1 = new ArrayList<>(), tv2 = new ArrayList<>();
                for (int j = i; j < Notes.indexOf(Notes.get(i)) + 3; j++) {
                    try {
                        tv1.add(Notes.get(j));
                        tv2.add(Titles.get(j));
                    } catch (Exception e) {
                    }
                }
                AddTblRow(tv1.toArray(new TextView[tv1.size()]), tv2.toArray(new TextView[tv1.size()]));
                //1,2,3 notes, titles 2?1?3?
                //new Home().WriteLine("CheckRows\n"+tv1.get(0).getText()+"|"+tv1.get(1).getText()+"\n"+tv2.get(0).getText()+"|"+tv2.get(1).getText());
            }

            runOnUiThread(()->{ ((TextView) findViewById(R.id.home2Title)).setText("RemindFul"); });

        }).start();

        new Thread(() -> {
            //System.out.println("DV setting.."); //Skipping frames from this point ??  commenting out runonUI makes it skip before dv..
            for (HashMap<String, String> s : notes) {
                DetailedViewSetup(s.get(DH.TITLE) + "", s.get(DH.YMDHMS) + "-" + s.get(DH.ID));
            }
            }).start();

        DH.close();
    }

    private String RecentInputDate="";
    private void DetailedViewSetup(String title, String tag){

        boolean Changed=false;
        TypedArray ta = this.obtainStyledAttributes(new int[]{R.attr.Text,R.attr.NoteTextBorder, R.attr.DelHighlight});
        TableRow tr = new TableRow(this);
        TextView tv = new TextView(this);

        tv.setTextColor(ta.getColor(0,-1));

        String RID = tag.split("-")[0].substring(0, 8);
        //reversing string so DD first      YYYYMMDD
        RID = ""+RID.substring(6)+"/"+RID.substring(4,6)+"/"+RID.substring(0,4);

        TableRow.LayoutParams LP = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT); //To add margin where necessary

        if (! RID.equals(RecentInputDate)){
            //System.out.println("DV running changed..");
            RecentInputDate = RID; Changed=true;
            int Dp5ToPix = (int) Math.ceil( 5 * getResources().getDisplayMetrics().density );
            tv.setPadding(Dp5ToPix,Dp5ToPix,Dp5ToPix,Dp5ToPix);

            //Grab DD/MM/YYYY
            tv.setText(RID+""); //System.out.println("RID:" +RID);

            LP.setMargins(0,(int) Math.ceil( 5 * getResources().getDisplayMetrics().density ),0,0);
            tr.setBackgroundColor(ta.getColor(2,-1));

        }else{
            //System.out.println("DV running..");
            tv.setBackgroundColor(ta.getColor(1,-1));
            tv.setTypeface(null, Typeface.BOLD);

            tv.setPadding((int) Math.ceil( 15 * getResources().getDisplayMetrics().density ),0,(int) Math.ceil( 5 * getResources().getDisplayMetrics().density ),0);
            tv.setTag(""+tag);

            tv.setText(title+""); //System.out.println("Title:" +title);

            tv.setOnClickListener(this::OpenNote);
        }

        tr.addView(tv,new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));          //NEEDS WEIGHT TO DISPLAY?
        tr.setPadding(10,10,10,10);

        tr.setLayoutParams( LP );
        runOnUiThread(()->{ ((ViewGroup)findViewById(R.id.home2DvTable)).addView(tr); });

        ta.recycle();
        if(Changed){ DetailedViewSetup(title,tag); }

    }

    private float DPtoPixel(int DP){
        //PixeltoDP
        ////get resources from frag - fix
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,DP,getApplicationContext().getResources().getDisplayMetrics());
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        System.out.println("Back key pressed!");

        //Check if any frag open

        for(Fragment Frag : getSupportFragmentManager().getFragments()) {
            //System.out.println("NumOfFrags: "+getSupportFragmentManager().getFragments().size()+" | Frag up: "+Frag.isAdded()+" | ID:"+Frag.getId());
            if (Frag.isVisible()){
                System.out.println("REMOVING FRAG");
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.frag_in,R.anim.frag_out).remove(Frag).commit();
                return;
            }
        }
        System.out.println("Backing activity");
        startActivity(new Intent(this, Home.class)
            , ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.activity_in,R.anim.activity_out).toBundle()
        );
    }

    //programatic anim  https://stackoverflow.com/questions/38594677/how-to-make-animation-programmatically
    private float DvOpen=0, PCT=DvOpen, TouchX=PCT, TouchY=PCT;

    private boolean CustTouchEvent(View v,MotionEvent event) {

        ; //TODO less clunky gesture

        // v = first one that was touched-down
        //https://developer.android.com/develop/ui/views/touch-and-input/gestures/detector#capture-touch-events-for-an-activity-or-view

        //System.out.println("event: "+event);
        //System.out.println("X:"+event.getX()+" Y:"+event.getY()+" | "+getResources().getDisplayMetrics().widthPixels+":"+getResources().getDisplayMetrics().heightPixels);
        //System.out.println( "1dp = "+ 1 * getResources().getDisplayMetrics().density );
        //Y is greater as goes down , X is greater as goes right

        //Down = click down | Up = release | Move = down + move
        //Only detects when starting from top of activity??
        ViewGroup Dv = findViewById(R.id.home2DvBg);

        //super.onTouchEvent(event); //Drags/scroll works as normal.. cant drag DV --- differentiate based on whever X or Y is > 300 first ?

        System.out.println("View: "+ v.getClass().getName() );

        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN): //System.out.println("Mdown");
                TouchX = event.getRawX(); TouchY=event.getRawY();
                System.err.println(TouchX+":"+TouchY);
                break;
            case (MotionEvent.ACTION_MOVE): //System.out.println("Mmove"); //InvalidPointerID when cant scroll
                //Compare

                //System.out.println("Gx: " + event.getRawX() + " Tx:" + TouchX);

                //Increase movement before appearing.. TX is 0 on first scroll ?? not possible
                if( TouchY==0 && TouchX==0 ){ return false; } //ERR with updating touch sometimes
                /*if ( Math.abs(event.getRawX() - TouchX) < 100 && Math.abs(event.getRawY() - TouchY) < 100) {
                    break;
                }*/

                else if (Math.abs(event.getRawX() - TouchX) < 200 && v.getClass() == ScrollView.class && Math.abs(event.getRawY() - TouchY) >= 1*getResources().getDisplayMetrics().density){
                    //System.out.println("ScrollView drag");
                    //Has to return or drag wont work

                    Dv.setAlpha(0); Dv.setTranslationX(getResources().getDisplayMetrics().widthPixels * -1);
                        //Forcefully hides DV

                    return super.onTouchEvent(event); //return super for scroll innate touch-event to override my custom event
                }

                float DecimalPct = 0.08f;

                if (DvOpen == 0) {
                    PCT = ((event.getRawX() - TouchX) * (getResources().getDisplayMetrics().density) * DecimalPct);
                    PCT = (PCT > 100) ? 100 : (PCT < 0) ? 0 : PCT;
                    Dv.setAlpha(PCT / 100);

                        //0 = all on screen so *-1 hides it all.. then move it by percentage
                    Dv.setTranslationX(getResources().getDisplayMetrics().widthPixels * -1 + getResources().getDisplayMetrics().widthPixels / 100f * PCT);
                } else if (DvOpen == 1) {
                    // *-1 doesnt work.. make it go from 100 to 0
                    PCT = ((event.getRawX() - TouchX) * (getResources().getDisplayMetrics().density) * DecimalPct) *-1;
                    PCT = (PCT > 100) ? 100 : (PCT < 0) ? 0 : PCT;
                        //changed this to include neg as well
                    PCT = 100 - PCT; //Reverses PCT to go 100=>0
                    Dv.setAlpha(PCT / 100);

                        //  0 = on screen   -   by PCT of screen width
                    Dv.setTranslationX(0 - getResources().getDisplayMetrics().widthPixels / 100f * (100-PCT));
                }

                //System.out.println("Dv open:" + DvOpen + " | " + PCT + "%");


                //make neg percent go back for covering thing not straight off screen

                break;
            case (MotionEvent.ACTION_UP): //System.out.println("Mup");
                //System.err.println("Get: " + event.getRawX() + ":"+event.getRawY()+"\nTou: " + TouchX+":"+TouchY);

                if ( Math.abs(event.getRawX() - TouchX) <= 1*getResources().getDisplayMetrics().density && Math.abs(event.getRawY() - TouchY) <= 1*getResources().getDisplayMetrics().density) {
                    //System.out.println("Onclick!! v:"+v.getClass());
                    v.performClick(); //ontouch interferes with click actions
                    return true;
                } else if (Math.abs(event.getRawX() - TouchX) < 200 && v.getClass() == ScrollView.class && Math.abs(event.getRawY() - TouchY) >= 300){
                    //System.out.println("ScrollView drag");

                    Dv.setTranslationX( (DvOpen==0) ? getResources().getDisplayMetrics().widthPixels * -1 : 0 );
                    return false;
                }


                //If CurrX ~ = 100% .. new frag? new animation play else undo
                if (DvOpen == 0) {
                    if (PCT >= 60) {
                        PCT = 100;
                        DvOpen = 1;

                        Dv.setTranslationX(0);
                        Dv.setAlpha(1);
                    } else {
                        PCT = 0;
                        //Undo anim..
                        Dv.setTranslationX(getResources().getDisplayMetrics().widthPixels * -1);
                        Dv.setAlpha(0);
                    }
                } else if (DvOpen == 1) {
                    if (PCT <= 45) {
                        PCT = 0;
                        DvOpen = 0;

                        Dv.setTranslationX(getResources().getDisplayMetrics().widthPixels * -1);
                        Dv.setAlpha(0);
                    } else {
                        PCT = 100;
                        //Undo anim..
                        Dv.setTranslationX(0);
                        Dv.setAlpha(1);
                    }
                }
                break;
            default:
                System.out.println("Diff motion: "+event); //cancel
                break;
        }

        return true;//return super.onTouchEvent(event); //false stops event from continuing detection
    }

}
