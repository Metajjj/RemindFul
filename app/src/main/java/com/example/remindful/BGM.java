package com.example.remindful;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class BGM extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SetupPermGrabber();

        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide(); //Hides default header
        setContentView(R.layout.bgm);

        Toast.makeText(this,"Needs permission to find and play your songs!",Toast.LENGTH_LONG).show();

        GrabPerms();
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
        System.out.println("======"); System.out.println(FileList.toString());
        FileList = new ArrayList<>();
        GrabMfiles(Environment.getRootDirectory()); //Appears to access system reserved storage
        System.out.println("======"); System.out.println(FileList.toString());
    }

    //TODO NPE for rootdir
    private void GrabMfiles(File F){
        if(F.isDirectory() && F.listFiles() != null){
            for(File f : F.listFiles()){
                if (f.isFile() && f.getName().endsWith(".mp3") | f.getName().endsWith(".mp4") | f.getName().endsWith(".ogg") | f.getName().endsWith(".wav")){
                    //System.out.println("File: "+f.getName());
                    FileList.add(f.getName()+"|"+f.getAbsolutePath());
                }else{
                    GrabMfiles(f);
                }
            }
        }
    }
}


