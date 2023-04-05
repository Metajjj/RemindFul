package com.example.remindful;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.riversun.promise.SyncPromise;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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

        //CLEARING OLD FROM REFRESH
        TableRow TR = (TableRow) getActivity().findViewById(R.id.DelFragSelAll).getParent();
        TL.removeAllViews(); TL.addView(TR);

        SyncPromise.resolve().always((action, data)->{
            ////GRAB ID + YMD TO USE AS TAG
            ArrayList<HashMap> S = DH.Readquery(
                    MessageFormat.format("SELECT `{0}`,`{2}`,`{3}` FROM `{1}`;",
                            DH.TITLE,DH.DBname,DH.YMDHMS,DH.ID)
            );

            //System.out.println("====\n"+S+"\n====");

            for (HashMap<String,String> x : S) {

                String PureTitle = x.get(DH.TITLE),
                        PureYMD = x.get(DH.YMDHMS),
                        PureID = x.get(DH.ID);

                System.out.println(MessageFormat.format(
                        "m1: {0} | m2: {1} | m3: {2}",
                        PureTitle, PureYMD, PureID));

                TableRow Tr = SetupRow(PureTitle);
                Tr.setTag(PureYMD + "-" + PureID);
                TL.addView(Tr);
            }

            action.resolve();
        }).then((a,d)->{

            ((TextView)getActivity().findViewById(R.id.DelFragSelAll)).setText("Invert Selection");
            ((View)(getActivity().findViewById(R.id.DelFragSelAll)).getParent()).setOnClickListener(this::DelFragTopButtClicked);

            a.resolve();
        }).start();
    }

    private float DP2Pixel(float DP){
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
        Tv.setTextSize( ((TextView)getActivity().findViewById(R.id.DelFragSelAll)).getTextSize() ); ////FIX app:autoSizeTextType="uniform"
            ////Messes up on refresh
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
        TextView tv = (TextView) v;

        String tv2 = tv.getText() +"";
        tv.setText("DELETED!");
        tv.setBackgroundColor(Color.rgb(50,201,94));

        new Handler().postDelayed(() -> {
            tv.setText(tv2);
            tv.setBackgroundResource(R.drawable.roundbordernote);
        },1300);

        //Check all checked => del them from DB - exclude top
        TableLayout TL = requireActivity().findViewById(R.id.DelFragTable);
        ArrayList<ArrayList<String>> ToBeDel = new ArrayList<>();
        for(int i=1;i<TL.getChildCount();i++){
            TableRow TR = (TableRow) TL.getChildAt(i);
            if( ((CheckBox) TR.getChildAt(1)).isChecked() ){
                String Title,YMD,ID;
                //True = del
                Title = ""+((TextView)TR.getChildAt(0)).getText();
                YMD = TR.getTag().toString().split("-")[0];
                ID = TR.getTag().toString().split("-")[1];
                ToBeDel.add(new ArrayList<>(Arrays.asList(ID,Title,YMD)));
            }
        }
        //System.out.println( ToBeDel );

        if(ToBeDel.size() > 0){
            QuickDelFormat(ToBeDel);
        }else{
            Toast.makeText(getContext(), "Nothing selected to delete!", Toast.LENGTH_SHORT).show();
        }

        ////REFRESH UI
        onStart();
    }

    private void QuickDelFormat(ArrayList<ArrayList<String>> ToBeDel) {
        //ID|Title|YMD
        final DatabaseHandler DH = new DatabaseHandler(getContext());

        ArrayList<String> Args=new ArrayList<>();

        String Query = "";
        for (ArrayList<String> AL : ToBeDel) {
            //ID,TITLE,YMD
            Query +=
                    MessageFormat.format(
                            "( {0} = {1} AND {2} = {3} AND {4} = {5} )",
                            DH.ID, "?", DH.TITLE, "?", DH.YMDHMS, "?"
                    );
            Args.add(AL.get(0));Args.add(AL.get(1));Args.add(AL.get(2));
            if (AL == ToBeDel.get(ToBeDel.size() - 1)) {
                Query += "";
            } else {
                Query += " OR ";
            }
        }
        System.out.println(Query);
        //DH.Writequery(Query);
        Toast.makeText(getContext(), "DELETED SELECTED!", Toast.LENGTH_SHORT).show();

        System.out.println("Num of rows deleted: "+
        DH.getWritableDatabase().delete(DH.DBname,Query,
                Args.toArray(new String[]{}))
        );
    }

    public void CloseFrag(View v){

        //null ptr except - activity
        //new Home2().Switchy( MainContainer.findViewById(R.id.home2ViewStyle) );

        getParentFragmentManager().beginTransaction().remove(DeleteFragment.this).commit();
        //getActivity().findViewById(R.id.home2FragHolder).back
        startActivity(new Intent(getContext(),Home2.class));
    }
}
