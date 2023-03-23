package com.example.remindful;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
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

        ((TextView)getActivity().findViewById(R.id.DelFragSelAll)).setText("Loading...");

        Promise.resolve().then((action,data)->{

            //Add stuff - append click to them during creation?

            action.resolve();
        }).then((a,d)->{

            ((TextView)getActivity().findViewById(R.id.DelFragSelAll)).setText("Select All");
            ((View)((TextView)getActivity().findViewById(R.id.DelFragSelAll)).getParent()).setOnClickListener(this::DelFragTopButtClicked);

            a.resolve();
        }).start();
    }

    private TableRow SetupRow(String Title){
        TextView Tv = new TextView(getContext()); CheckBox Cb = new CheckBox(getContext()); TableRow Tr = new TableRow(getContext()); ViewGroup.LayoutParams Params;

        ///app:autoSizeTextType="uniform"

        //TextView
        Params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,0);
        Tv.setPadding((int)Math.floor(new Home2().DPtoPixel(3)),(int)Math.floor(new Home2().DPtoPixel(3)),(int)Math.floor(new Home2().DPtoPixel(3)),(int)Math.floor(new Home2().DPtoPixel(3)));

        Tv.setSingleLine(true); Tv.setMaxLines(1);
        Tv.setGravity(Gravity.CENTER); Tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Tv.setTypeface(null, Typeface.BOLD);
        Tv.setLayoutParams(Params); Tv.setText(Title);

        new AttributeSet().getAttributeIntValue("","",0);
        TypedValue;


        //CheckBox
        Params = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT,1);
        Cb.setLayoutParams(Params); Cb.setClickable(false);

        //TableRow
        Tr.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Tr.addView(Tv,0);Tr.addView(Cb,1);
        Tr.setOnClickListener(this::DelFragButtClicked);


        return Tr;
    }

    public void DelFragTopButtClicked(View v){
        TableLayout TL = (TableLayout) getActivity().findViewById(R.id.DelFragTable);
        for(int i=1;i<TL.getChildCount();i++){
            DelFragButtClicked( TL.getChildAt(i) );
        }
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

        ((TextView)tr.getChildAt(0)).setTextColor(Color.parseColor(TextCol));
    }
}
