package com.example.remindful;

import android.Manifest;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class BGM extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SetupPermGrabber();

        setTheme(new Home().Themes.get(new Home().ThemeNum));

        MediaBGM.setLooping(true); //Loop true for media

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header
        setContentView(R.layout.bgm);

        Toast.makeText(this,"Needs permission to find and play your songs!",Toast.LENGTH_LONG).show();

        GrabPerms();

        findViewById(R.id.BGM_URI).setOnClickListener(this::ShowFullPath);

        new Handler().post(()->{
            for (String fpath: FileList ) { SetupRow(fpath); }
        });
    }

    ActivityResultLauncher<String> ARL;

    private void SetupPermGrabber(){
        //New way of checking permission - has to be created before fragment is
        ARL = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                res -> {
                    if (!res) {
                        //Not granted!
                        Toast.makeText(getApplicationContext(), "Need perm to work!", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void GrabPerms(){
        //Grab perms even if alrdy given

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ARL.launch(Manifest.permission.READ_MEDIA_VIDEO);
            ARL.launch(Manifest.permission.READ_MEDIA_AUDIO);
        }
        ARL.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

        GrabMusic();
    }

    private ArrayList<String> FileList = new ArrayList<>();

    private void GrabMusic(){
        System.out.println(
            Environment.getExternalStorageDirectory().toString() +"|"+ Environment.getRootDirectory()
        );

        GrabMfiles(Environment.getExternalStorageDirectory()); //Appears to get the general files
        System.out.println(FileList.toString());
        //FileList = new ArrayList<>();
        //GrabMfiles(Environment.getRootDirectory()); //Appears to access system reserved storage
        //System.out.println(FileList.toString());
    }

    private void GrabMfiles(File F){
        if(F.isDirectory() && F.listFiles() != null){
            for(File f : F.listFiles()){
                if (f.isFile() && f.getName().endsWith(".mp3") | f.getName().endsWith(".mp4") | f.getName().endsWith(".ogg") | f.getName().endsWith(".wav")){
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
        TV1.setText(Txt);
        TV1.setOnClickListener(this::ShowFullPath);
        TV1.setOnLongClickListener(v->{
            ShowFullPath(v);
            return true;
        });

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
        TV2.setTextColor(TxtCol); ////todo doesnt work changing col for symbols -- all blue
        TV2.setText( (CurrSong.equals(Txt)) ? PauseSymbol : PlaySymbol); //Make pause symbol if playing song
        TV2.setOnClickListener(this::PlayPause);

        (ResourcesCompat.getDrawable(getResources(),R.drawable.tri,getTheme())).setColorFilter(TxtCol, PorterDuff.Mode.SRC_IN);
        TV2.setBackgroundResource(R.drawable.tri);


        //TV2.setColorFilter( TxtCol, android.graphics.PorterDuff.Mode.SRC_IN );

        //Add to TL
        TR.addView(TV1,0);TR.addView(TV2,1); //TR alrdy has child ??????
        TableLayout TL = findViewById(R.id.BGM_Table);
        TL.addView(TR); //TL alrdy has child ??????

        ta.recycle();
    }

    private void ShowFullPath(View v){
        TextView tv = (TextView) v;
        Toast.makeText(this,tv.getText(),Toast.LENGTH_SHORT).show();
    }

    private final static MediaPlayer MediaBGM = new MediaPlayer();
    private static String CurrSong=""; //Static to keep track when activity closed
    private String PlaySymbol="▶", PauseSymbol="⏸"; //▶️ ⏸️
    //todo make drawable??

    private void PlayPause(View v){
        v.setOnClickListener(null); //Avoid spamming and other potential problems during media change

        TypedArray ta = this.obtainStyledAttributes(new int[]{R.attr.Title});
        TextView tv = (TextView) v;
        String songLoc = ((TextView)((TableRow)tv.getParent()).getChildAt(0)).getText().toString(); //Filepath always left/first

        //todo prepare for starting mult song

        if(tv.getText().toString().equals(PlaySymbol)){
            //Loop and make everything else a play symbol
            TableLayout TL = findViewById(R.id.BGM_Table);
            for(int i=1;i<TL.getChildCount();i++){
                TableRow TR = (TableRow) TL.getChildAt(i);
                TextView TV2 = (TextView) TR.getChildAt(1);
                TV2.setText(PlaySymbol);
                TV2.setTextColor(ta.getColor(0,-1) );
                System.out.println("MP reset all symbols");
            }


            tv.setText(PauseSymbol);

            if(MediaBGM.isPlaying()){
                MediaBGM.stop(); MediaBGM.reset();
                System.out.println("MP stop + reset");
            }

            //If isnt curr paused, prepare it!
            if(! CurrSong.equals(songLoc) ){
                try {
                    MediaBGM.reset();

                    MediaBGM.setDataSource(songLoc); MediaBGM.prepare();
                    System.out.println("MP data source + prepare");
                    CurrSong=songLoc;
                    MediaBGM.start();
                } catch (Exception e) {
                    System.err.println(""+e); Toast.makeText(this, "Error occured playing, possibly file moved!\nRecommended to re-enter page", Toast.LENGTH_SHORT).show();
                }
            }else{
                MediaBGM.start(); //Continue if curr song
                System.out.println("MP resume");
            }
        }else{
            tv.setText(PlaySymbol);
            tv.setTextColor( ta.getColor(0,-1) );
            //Remove/pause song from media player
            MediaBGM.pause();
            System.out.println("MP pause");
        }

        ta.recycle();
        v.setOnClickListener(this::PlayPause);
    }
}


