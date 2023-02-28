package com.example.remindful;

import android.Manifest;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

public class BGM extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        DatabaseHandler dh = new DatabaseHandler(getApplicationContext());
        dh.close();
        super.onDestroy();
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SetupPermGrabber();

        setTheme(Home.Themes.get(Home.ThemeNum));

        MediaBGM.setLooping(true); //Loop true for media

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header
        setContentView(R.layout.bgm);

        ((TextView)findViewById(R.id.BGM_title)).setText("Loading...");

        findViewById(R.id.BGM_URI).setOnClickListener(this::ShowFullPath);
    }

    //Setting custom anims for each activity fired
    @Override
    public void startActivity(Intent i) { super.startActivity(i); overridePendingTransition(R.anim.activity_in,R.anim.activity_out); }
    @Override
    public void startActivity(Intent i, @Nullable Bundle o) { super.startActivity(i, o); overridePendingTransition(R.anim.activity_in,R.anim.activity_out); }

    private ActivityResultLauncher<String> ARL;

    private void SetupPermGrabber(){
        //New way of checking permission - has to be created before fragment is
        ARL = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                res -> {
                    if (!res) {
                        //Not granted!
                        Toast.makeText(getApplicationContext(), "Need perm to work!", Toast.LENGTH_LONG).show();
                    } else{
                        Toast.makeText(getApplicationContext(), "May need to reload activity if first time enabling perm!", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        try {
            BufferedReader bfr = new BufferedReader( new FileReader( new File(getApplicationContext().getFilesDir(), "F")) );
            String l = bfr.readLine(); bfr.close();

            //currtheme is manifest theme ?? how grab activity theme..
            if (getResources().getIdentifier(l,"style",getPackageName()) != Home.Themes.get(Home.ThemeNum)){
                Toast.makeText(getApplicationContext(),"Theme lost, reloading!", Toast.LENGTH_SHORT).show();
                recreate(); //Restart activity if theme not same
            }
            //Toast.makeText(getApplicationContext(),"CurrTheme: " + getResources().getIdentifier(l,"style",getPackageName()) + "\nFileTheme: " + Home.Themes.get(Home.ThemeNum),Toast.LENGTH_LONG).show();
        }catch (Exception e){ System.err.println("ERR: "+e);}

        super.onStart();

        //for(int i=1;i<((TableLayout)findViewById(R.id.BGM_Table)).getChildCount();i++){ ((TableLayout)findViewById(R.id.BGM_Table)).removeViewAt(i); }

        GrabPerms();
    }

    private void GrabPerms(){
        //Grab perms even if alrdy given

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ARL.launch(Manifest.permission.READ_MEDIA_VIDEO);
            ARL.launch(Manifest.permission.READ_MEDIA_AUDIO);
        }
        ARL.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

        //Avoids dupe songs from reentering app on BGM page
        if (((TableLayout)findViewById(R.id.BGM_Table)).getChildCount() <=1){ GrabMusic(); }
    }

    private ArrayList<String> FileList = new ArrayList<>();

    private void GrabMusic() {
        //System.out.println( Environment.getExternalStorageDirectory().toString() +"|"+ Environment.getRootDirectory() );

        //Using thread works better, runOnUiThread for handler type stuff
        new Thread(() -> {
            //System.out.println("UI thread? "+ (Looper.getMainLooper().getThread() == Thread.currentThread()));
            //is 2nd thread

        ///new Handler().post(() -> {

            //try { Thread.sleep(2000); } catch (Exception e) { }

            GrabMfiles(Environment.getExternalStorageDirectory()); //Appears to get the general files
            //System.out.println(FileList.toString());
            //FileList = new ArrayList<>();
            //GrabMfiles(Environment.getRootDirectory()); //Appears to access system reserved storage
            //System.out.println(FileList.toString());

            for (String fpath : FileList) {
                ///new Handler().post(() -> { //Individual handle for each setup removes bottleneck

                    //Handlers are main UI thread
                    //System.out.println(Looper.getMainLooper().getThread() == Thread.currentThread());

                runOnUiThread(()->{
                    SetupRow(fpath);
                    String s = "Loading...\n"+ (FileList.indexOf(fpath) + 1) + "/" + FileList.size();

                    ((TextView)findViewById(R.id.BGM_title)).setText(s);
                    //System.out.println(s);
                });

                try{ Thread.sleep(20); }catch (Exception e){}

                ///});
            } //Moved from onCreate to not delay main thread
        ///});
            runOnUiThread(()->{((TextView)findViewById(R.id.BGM_title)).setText("Background Music");});
        }).start();
    }

    //todo sort so it only finds in "music" folders?
    private void GrabMfiles(File F){

        if(F.isDirectory() && F.listFiles() != null){
            for(File f : F.listFiles()){
                if (f.isFile() && f.getName().endsWith(".mp3") | f.getName().endsWith(".mp4") | f.getName().endsWith(".ogg") | f.getName().endsWith(".wav") &&

                        /*REGEX match*/
                        Pattern.compile("music",Pattern.CASE_INSENSITIVE).matcher(F.getAbsolutePath()) .find()
                ){
                    //System.out.println("File: "+f.getName());
                    if (! FileList.contains(f.getAbsolutePath()) ) { FileList.add(f.getAbsolutePath()); }
                }else{
                    GrabMfiles(f);
                }
            }
        }
    }

    private void SetupRow(String Txt){
        TableRow TR = new TableRow(this);
        TextView TV1 = new TextView(this), TV2= new TextView(this);

        //TableRow
        TR.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TypedArray ta = this.obtainStyledAttributes(new int[]{R.attr.Title});
        int TxtCol = ta.getColor(0,-1);

        //TV1
        TV1.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,2));
        TV1.setSingleLine(true);
        TV1.setMaxLines(1);
        TV1.setTypeface(null, Typeface.BOLD);
        int DP8padPx= (int) Math.ceil(
                8 * getResources().getDisplayMetrics().density
        );
        TV1.setPadding(DP8padPx,DP8padPx,DP8padPx,DP8padPx);
        TV1.setTextSize( (int) Math.ceil(
                5 * getResources().getDisplayMetrics().density )
        );
        TV1.setGravity(Gravity.CENTER); TV1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        TV1.setTextColor(TxtCol);
        TV1.setEllipsize(TextUtils.TruncateAt.START);
        TV1.setText( Txt.split("/") [ Txt.split("/").length -1 ] );
        TV1.setOnClickListener(this::ShowFullPath);
        TV1.setOnLongClickListener(v->{
            ShowFullPath(v);
            return true;
        });
        //marquee
        TV1.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        TV1.setMarqueeRepeatLimit(-1); //-1 = infinite
        TV1.setSelected(true);


        //TV2
        TV2.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,0.5f));
        TV2.setSingleLine(true);
        TV2.setMaxLines(1);
        TV2.setTypeface(null, Typeface.BOLD);
        TV2.setPadding(DP8padPx,DP8padPx,DP8padPx,DP8padPx);
        //float PxToDP = 20 * getResources().getDisplayMetrics().density; //System.out.println(PxToDP+" : DP == 20px"); 55dp
        TV2.setTextSize(
                55 / getResources().getDisplayMetrics().density
        );
        TV2.setGravity(Gravity.CENTER); TV2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        //TV2.setTextColor(TxtCol);
        //TV2.setText( (CurrSong.equals(Txt)) ? PauseSymbol : PlaySymbol); //Make pause symbol if playing song
        TV2.setOnClickListener(this::PlayPause);

        //Setting up drawable resource
        (ResourcesCompat.getDrawable(getResources(),R.drawable.tri,getTheme())).setColorFilter(TxtCol, PorterDuff.Mode.SRC_IN);
        (ResourcesCompat.getDrawable(getResources(),R.drawable.octa,getTheme())).setColorFilter(TxtCol, PorterDuff.Mode.SRC_IN);

        TV2.setBackgroundResource( (CurrSong.equals(Txt)) ? R.drawable.octa : R.drawable.tri );

        //Add to TL
        TR.addView(TV1, 0); TR.addView(TV2, 1);
        TableLayout TL = findViewById(R.id.BGM_Table);
        TL.addView(TR);

        ta.recycle();
    }

    private void ShowFullPath(View v){
        TextView tv = (TextView) v;
        Toast.makeText(this,tv.getText(),Toast.LENGTH_SHORT).show();
    }

    private final static MediaPlayer MediaBGM = new MediaPlayer();
    private static String CurrSong=""; //Static to keep track when activity closed
    //private String PlaySymbol="▶", PauseSymbol="⏸"; //▶️ ⏸️

    private Bitmap DrwBtmp(Drawable dr){
        if(dr instanceof BitmapDrawable){ return ((BitmapDrawable) dr).getBitmap(); }

        Bitmap Res;
        int w= (dr.getIntrinsicWidth()<=0) ? 1 : dr.getIntrinsicWidth(), h = (dr.getIntrinsicHeight()<=0) ? 1 : dr.getIntrinsicHeight();

        Res = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        Canvas C = new Canvas(Res);
        dr.setBounds(0,0,C.getWidth(),C.getHeight());
        dr.draw(C);

        new Thread(System::gc).start(); //Memory intensive when dealing with bitmaps  best to preemptively deallocate

        return Res;
    }

    private void PlayPause(View v){
        v.setOnClickListener(null); //Avoid spamming and other potential problems during media change

        TypedArray ta = this.obtainStyledAttributes(new int[]{R.attr.Title});
        TextView tv = (TextView) v;
        String songLoc = ((TextView)((TableRow)tv.getParent()).getChildAt(0)).getText().toString(); //Filepath always left/first

        //// prepare for starting mult song ?

        Drawable dr1 = tv.getBackground(), dr2 = ResourcesCompat.getDrawable(getResources(),R.drawable.tri,getTheme());
        //System.out.println("TvBg: "+dr1+" | Res: "+dr2.toString()+"\n Compared: "+CompareDrawables(dr1,dr2);
        //System.out.println("Comparing bit: "+ DrwBtmp(dr1).sameAs( DrwBtmp(dr2) ) );

        if( DrwBtmp(dr1).sameAs( DrwBtmp(dr2) ) ){
            //Loop and make everything else a play symbol
            TableLayout TL = findViewById(R.id.BGM_Table);
            for(int i=1;i<TL.getChildCount();i++){
                TextView TV2 = (TextView) ((TableRow) TL.getChildAt(i)).getChildAt(1);

                TV2.setBackgroundResource(R.drawable.tri);
                //TV2.setTextColor(ta.getColor(0,-1) );
            }


            tv.setBackgroundResource(R.drawable.octa);

            if(MediaBGM.isPlaying()){
                MediaBGM.stop(); MediaBGM.reset();
                //System.out.println("MP stop + reset");
            }

            //If isnt curr paused, prepare it!
            if(! CurrSong.equals(songLoc) ){
                try {
                    MediaBGM.reset();

                    MediaBGM.setDataSource(songLoc); MediaBGM.prepare();
                    //System.out.println("MP data source + prepare");
                    CurrSong=songLoc;
                    MediaBGM.start();

                    MediaBGM.setLooping(true);
                } catch (Exception e) {
                    System.err.println(""+e); Toast.makeText(this, "Error occured playing, possibly file moved!\nRecommended to re-enter page", Toast.LENGTH_SHORT).show();
                }
            }else{
                MediaBGM.start(); //Continue if curr song
                //System.out.println("MP resume");
            }
        }else{
            tv.setBackgroundResource(R.drawable.tri);
            //tv.setTextColor( ta.getColor(0,-1) );
            //Remove/pause song from media player
            MediaBGM.pause();
            //System.out.println("MP pause");
        }

        ta.recycle();
        v.setOnClickListener(this::PlayPause);
    }

    public void onBackPressed() {
        System.out.println("Back key pressed!");

        super.onBackPressed();

        //Check if any frag open

        for(Fragment Frag : getSupportFragmentManager().getFragments()) {
            //System.out.println("NumOfFrags: "+getSupportFragmentManager().getFragments().size()+" | Frag up: "+Frag.isAdded()+" | ID:"+Frag.getId());
            if (Frag.isVisible()){
                //System.out.println("REMOVING FRAG");
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.frag_in,R.anim.frag_out).remove(Frag).commit();
                return;
            }
        }
        //System.out.println("Backing activity");
        startActivity(new Intent(this, Home2.class));
        overridePendingTransition(R.anim.activity_in,R.anim.activity_out);
    }
}


