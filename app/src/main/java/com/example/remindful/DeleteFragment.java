package com.example.remindful;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.riversun.promise.Promise;

public class DeleteFragment extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //new Home().WriteLine(""+container.getClass().getName() ); //FRAMELAYOUT

        return inflater.inflate(R.layout.delete_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //Promise = new handler  -  avoid ui locking and waits
        //.always takes both reject and resolve //SyncPromise makes it wait for first function before next if multithreaded..
        System.out.println("Promise BEGIN");
        Promise.resolve().then((action,data)->{
            System.out.println("Promise1 BEGIN");

                System.out.println("Promise1 SLEEP - BEGIN");
                for(int i=0;i<5;i++){
                    try{ Thread.sleep(1000); } catch (Exception e){ System.out.println("ERR: "+e);}
                }
                System.out.println("Promise1 SLEEP - END");


            System.out.println("Promise1 END");
            action.resolve();
        }).then((a,d)->{
            System.out.println("Promise2 BEGIN");

            TableLayout TL = getActivity().findViewById(R.id.DelFragTable);
            for(int i=0;i<TL.getChildCount();i++){
                TL.getChildAt(i).setOnClickListener(this::DelFragButtClicked);

                //TableRow TR = (TableRow) TL.getChildAt(i);
                /*for(int j=0;j<TR.getChildCount();j++){
                    TR.getChildAt(j).setOnClickListener(this::DelFragButtClicked);
                }*/
            }

            System.out.println("Promise2 END");
            a.resolve();
        }).start();
        System.out.println("Promise END");
    }

    public void DelFragButtClicked(View v){
        //default set background to drawable.. on click make whole bg same col.. change txt from brown to yellow
        TableRow tr = (TableRow) v;
        String TextCol="";
        CheckBox cb = (CheckBox) tr.getChildAt(1);
        cb.setChecked( !cb.isChecked() );

        if (cb.isChecked()){
            TextCol="#f4d882";
            tr.getChildAt(0).setBackgroundColor(Color.parseColor("#660000"));
        }else{
            TextCol="#6C5346";
            tr.getChildAt(0).setBackgroundResource(R.drawable.roundborderdel);
        }
        //chvck box then ...
        ((TextView)tr.getChildAt(0)).setTextColor(Color.parseColor(TextCol));

        //if row is index 0.. set all rows to ticked... // if txt = SELECT ALL
        if(v == ((ViewGroup)v.getParent()).getChildAt(0)){
            new Home().WriteLine("is top row!"); //WORKS
        }
    }
}
