package com.example.remindful;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MenuFragment extends DialogFragment {

    private Context context;

    @Override @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        context = getContext().getApplicationContext();

        //TransitionInflater TI = TransitionInflater.from(context); setEnterTransition(TI.inflateTransition(R.transition.transition)); setExitTransition(TI.inflateTransition(R.anim.frag_out));
        //Animations give more control than transitions

        return inflater.inflate(R.layout.menu_fragment, container, false);
    }

    DatabaseHandler DH;

    @Override
    public void onStart() {
        super.onStart();

        DH = new DatabaseHandler(context);

        //onClick FUNCTIONS

        //Remove fragment and updates activity
        getActivity().findViewById(R.id.MenuFragBg).setOnClickListener(v-> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.frag_in, R.anim.frag_out)
                    .remove(MenuFragment.this).commit();

            FrameLayout FL = getActivity().findViewById(R.id.home2FragHolder);
            ((ViewGroup)FL.getParent()).removeView(FL); ((ViewGroup)getActivity().findViewById(R.id.home2Bg)).addView(FL,0);
            //Pushes framelayout to back -- has to be index 0 so those next in line overwrite it

            startActivity(new Intent(context, Home2.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                , ActivityOptions.makeCustomAnimation(context,R.anim.activity_in,R.anim.activity_out).toBundle()
            );
        } );

        getActivity().findViewById(R.id.MenuFragMenu).setOnClickListener(null);

        //Creates new note
        getActivity().findViewById(R.id.MenuFragNewNote).setOnClickListener(v-> {

            getActivity().findViewById(R.id.MenuFragBg).performClick();


            startActivity(new Intent(context,NewNote.class)
                , ActivityOptions.makeCustomAnimation(context,R.anim.activity_in,R.anim.activity_out).toBundle()
            );
        } );

        //Opens del frag
        getActivity().findViewById(R.id.MenuFragWipeSel).setOnClickListener(v-> {
            Toast.makeText(context,"Del time!",Toast.LENGTH_LONG).show();

            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.frag_in, R.anim.frag_out)
                    .replace(R.id.home2FragHolder,DeleteFragment.class,null).commit();

            getActivity().findViewById(R.id.home2FragHolder).bringToFront();
        } );

        //Del all notes
        getActivity().findViewById(R.id.MenuFragWipeAll).setOnClickListener(v->
        {
            DatabaseHandler DH = new DatabaseHandler(context); DH.ResetTable(DH.getWritableDatabase()); DH.close();
            getActivity().findViewById(R.id.MenuFragBg).performClick();
            ((TextView)getActivity().findViewById(R.id.home2ViewStyle)).setText("Alphabetical");
            getActivity().findViewById(R.id.home2ViewStyle).performClick();
        });

        //BGM
        getActivity().findViewById(R.id.MenuFragMusic).setOnClickListener(v->{
            getActivity().findViewById(R.id.MenuFragBg).performClick();

            startActivity(new Intent(context, BGM.class)
                , ActivityOptions.makeCustomAnimation(context,R.anim.activity_in,R.anim.activity_out).toBundle()
            );
        });

        getActivity().findViewById(R.id.MenuFragDataMove).setOnClickListener(v->{

            Home.ARL.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

            new AlertDialog.Builder(getActivity())
                    .setTitle("Import or Export your notes?")
                    .setPositiveButton("Import", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Importing notes

                            ShowFiles(Environment.getExternalStorageDirectory());
                            //System.out.println("DP: "+DownloadPath);

                            if(DownloadPath == null){
                                Toast.makeText(getActivity(), "Didnt detect `download` or `downloads` folder\nMissing perm?", Toast.LENGTH_SHORT).show();
                                //Home.ARL.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                                return;
                            }
                            //Toast.makeText(context, "Download found!", Toast.LENGTH_SHORT).show();

                            //Grab download folder

                            if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                                Toast.makeText(context, "Need perm to read database from downloads", Toast.LENGTH_SHORT).show();
                                Home.ARL.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                            }


                            try{
                                FileInputStream FIS = new FileInputStream(DownloadPath+"/NoteDB.RemindFul");
                                FileOutputStream FOS = new FileOutputStream(DH.getReadableDatabase().getPath());

                                FIS.getChannel().transferTo(0,FIS.getChannel().size(),FOS.getChannel());
                                FIS.close(); FOS.close();

                                Toast.makeText(context, "DB imported!", Toast.LENGTH_LONG).show();

                                //System.out.println("DB caught: "+f.getAbsolutePath());
                            }
                            catch (Exception e){
                                Toast.makeText(context, "ERROR OCCURED", Toast.LENGTH_LONG).show();
                                System.out.println(e);
                            }

                        }
                        private String DownloadPath=null;
                        private void ShowFiles(File F){
                            if(F.isDirectory() && F.listFiles() != null) {
                                if(F.getName().equalsIgnoreCase("Download") || F.getName().equalsIgnoreCase("Downloads")){
                                    //System.out.println("Download folder: "+F.getAbsolutePath());
                                    DownloadPath = F.getAbsolutePath();
                                }else {

                                    for (File f : F.listFiles()) {
                                        if (f.isDirectory()) { ShowFiles(f); }
                                    }
                                }
                            }
                        }

                    })
                    .setNeutralButton("Export", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Exporting notes

                            ShowFiles(Environment.getExternalStorageDirectory());
                            //System.out.println("DP: "+DownloadPath);

                            if(DownloadPath == null){
                                Toast.makeText(getActivity(), "Didnt detect `download` or `downloads` folder\nMissing perm?", Toast.LENGTH_SHORT).show();
                                Home.ARL.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                                return;
                            }
                            //Toast.makeText(context, "Download found!", Toast.LENGTH_SHORT).show();

                            if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                                Toast.makeText(context, "Need perm to copy database to downloads", Toast.LENGTH_SHORT).show();
                                Home.ARL.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            }


                            try{
                                FileInputStream FIS = new FileInputStream(DH.getReadableDatabase().getPath());
                                FileOutputStream FOS = new FileOutputStream(DownloadPath+"/NoteDB.RemindFul");

                                FIS.getChannel().transferTo(0,FIS.getChannel().size(),FOS.getChannel());
                                FIS.close(); FOS.close();

                                Toast.makeText(context, "DB copied to Download folder", Toast.LENGTH_LONG).show();

                                //System.out.println("DB caught: "+f.getAbsolutePath());
                            }
                            catch (Exception e){
                                Toast.makeText(context, "ERROR OCCURED", Toast.LENGTH_LONG).show();
                                System.out.println(e);
                            }

                        }
                        private String DownloadPath=null;
                        private void ShowFiles(File F){
                            if(F.isDirectory() && F.listFiles() != null) {
                                if(F.getName().equalsIgnoreCase("Download") || F.getName().equalsIgnoreCase("Downloads")){
                                    //System.out.println("Download folder: "+F.getAbsolutePath());
                                    DownloadPath = F.getAbsolutePath();
                                }else {

                                    for (File f : F.listFiles()) {
                                        if (f.isDirectory()) { ShowFiles(f); }
                                    }
                                }
                            }
                        }
                    })
                    .show();
        });
        //TODO get a tmp pass to encrypt notes?
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        DH.close();
    }

    private ActivityResultLauncher<String> ARL;
}
