package com.example.remindful;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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

import org.riversun.promise.SyncPromise;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteFragment extends DialogFragment {
    //private final DatabaseHandler DH = new DatabaseHandler(getContext());
    // Err if ran before added to activity
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
        getActivity().findViewById(R.id.DelFragBg).setOnClickListener(this::CloseFrag);
        getActivity().findViewById(R.id.DelFragButt).setOnClickListener(this::DelFragDelBut);

        final DatabaseHandler DH = new DatabaseHandler(getContext());

        //Promise = new handler  -  avoid ui locking and waits
        //.always takes both reject and resolve //SyncPromise makes it wait for first function before next if multithreaded..

        ((TextView)getActivity().findViewById(R.id.DelFragSelAll)).setText("Loading...");
        TableLayout TL = (getActivity().findViewById(R.id.DelFragTable));

        System.out.println("P B");
        SyncPromise.resolve().always((action, data)->{
            ////GRAB ID + YMD TO USE AS TAG
            String s = DH.Readquery(
                    MessageFormat.format("SELECT `{0}`,`{2}`,`{3}` FROM `{1}`;",
                            DH.TITLE,DH.DBname,DH.YMDHMS,DH.ID)
            );

            //System.out.println("====\n"+s);
            //Figure out
            for (String x : s.split(Pattern.quote(DH.Seperator)) ) {
                Matcher m1 = Pattern.compile("Title:").matcher(x),
                m2 = Pattern.compile("YMDHMS:").matcher(x),
                m3 = Pattern.compile("ID:").matcher(x);
                if(m1.find() && m2.find() && m3.find()){
                    TableRow Tr = SetupRow( x.substring(m1.end()) );
                    Tr.setTag(0, x.substring(m2.end()) +"-"+ x.substring(m3.end()) );
                    TL.addView(Tr);
                }
            }


            action.resolve();
        }).then((a,d)->{

            ((TextView)getActivity().findViewById(R.id.DelFragSelAll)).setText("Invert Selection");
            ((View)(getActivity().findViewById(R.id.DelFragSelAll)).getParent()).setOnClickListener(this::DelFragTopButtClicked);

            a.resolve();
        }).start();
    }

    private float DP2Pixel(int DP){
        //PixeltoDP
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,DP,getResources().getDisplayMetrics());
    }

    private TableRow SetupRow(String Title){
        TextView Tv = new TextView(getContext()); CheckBox Cb = new CheckBox(getContext()); TableRow Tr = new TableRow(getContext()); ViewGroup.LayoutParams Params;

        //TextView
        Params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,0);
        //PadVal = (int)Math.floor(new Home2().DPtoPixel(3)); //Error to run new Activity class
        int PadVal = (int)Math.floor(DP2Pixel(3));
        Tv.setPadding(PadVal,PadVal,PadVal,PadVal);

        Tv.setSingleLine(true); Tv.setMaxLines(1);
        Tv.setGravity(Gravity.CENTER); Tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Tv.setTypeface(null, Typeface.BOLD);
        Tv.setLayoutParams(Params); Tv.setText(Title);
        Tv.setTextSize( ((TextView)getActivity().findViewById(R.id.DelFragSelAll)).getTextSize() ); ///app:autoSizeTextType="uniform"
        Tv.setBackgroundResource(R.drawable.roundborderdel); Tv.setTextColor(Color.parseColor("#6C5346"));

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
        TableLayout TL = getActivity().findViewById(R.id.DelFragTable);
        for(int i=0;i<TL.getChildCount();i++){
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

    public void DelFragDelBut(View v){
        final DatabaseHandler DH = new DatabaseHandler(getContext());

        //Check all checked => del them from DB - exclude top
        TableLayout TL = getActivity().findViewById(R.id.DelFragTable);
        ArrayList<String> ToBeDel = new ArrayList<>();
        for(int i=1;i<TL.getChildCount();i++){
            TableRow TR = (TableRow) TL.getChildAt(i);
            if( ((CheckBox) TR.getChildAt(1)).isChecked() ){
                //True = del
                ToBeDel.add(""+ ((TextView)TR.getChildAt(0)).getText() );
                System.out.println("TAG: "+ TR.getChildAt(0).getTag(0) );
            }
        }
        System.out.println( ToBeDel );
    }

    public void CloseFrag(View v){
        new Home2().Switchy( getActivity().findViewById(R.id.home2ViewStyle) );
        getParentFragmentManager().beginTransaction().remove(DeleteFragment.this).commit();
        //getActivity().findViewById(R.id.home2FragHolder).back
    }
}
