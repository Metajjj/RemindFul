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

public class BGM extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SetupPermGrabber();

        super.onCreate(savedInstanceState);



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

    private void GrabMusic(){
        System.out.println(
            Environment.getExternalStorageDirectory().toString() +"|"+ Environment.getRootDirectory()
        );
    }
}


