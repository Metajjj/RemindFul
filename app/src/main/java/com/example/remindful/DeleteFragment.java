package com.example.remindful;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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

        Toast.makeText(getActivity(),"Hold-click titles to get full name!",Toast.LENGTH_LONG).show();

        //onClick listeners

        //Removing fragment
        getActivity().findViewById(R.id.DelFragBg).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.frag_in, R.anim.frag_out)
                    .remove(DeleteFragment.this).commit();

            FrameLayout FL = getActivity().findViewById(R.id.home2FragHolder);
            ((ViewGroup)FL.getParent()).removeView(FL); ((ViewGroup)getActivity().findViewById(R.id.home2Bg)).addView(FL,0);

            startActivity(new Intent(getContext().getApplicationContext(), Home2.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
        });

        //Deleting from table in database and activity
        getActivity().findViewById(R.id.DelFragButt).setOnClickListener(this::DelFragDelBut);
        final DatabaseHandler DH = new DatabaseHandler(getContext());


        //Promise = new handler  -  avoid ui locking and waits
        //.always takes both reject and resolve //SyncPromise makes it wait for first function before next if multithreaded..

        TextView SelAllTv = getActivity().findViewById(R.id.DelFragSelAll);
        SelAllTv.setText("Invert Selection");
        TableLayout TL = (getActivity().findViewById(R.id.DelFragTable));

        //CLEARING OLD FROM REFRESH
        TableRow TR = (TableRow) SelAllTv.getParent();
        TL.removeAllViews(); TL.addView(TR);

        //needs post or getWidth is not what is drawn/finalised
        SelAllTv.post(()->{
            /*Rect bounds = new Rect(); Paint TextPaint = SelAllTv.getPaint();
            //TextPaint.getTextBounds(SelAllTv.getText(),0,SelAllTv.getText().length(),bounds);
            //int TxtH = bounds.height(), TxtW=bounds.width();
            //TextPaint doesnt include elipses

            /*System.out.println( MessageFormat.format(
                    "HANDLER: \nTop tv txtsize: {0}px | Top tv w: {1} | Top tv char count: {2} | TxtPaint: {3}",
                    SelAllTv.getTextSize(),
                    SelAllTv.getWidth() ,
                    SelAllTv.getText().length(),
                    TextPaint.measureText(SelAllTv.getText()+"")
            ));*/

            SelAllTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, SelAllTv.getTextSize() -20);

        });
        // TextSize (80px) vs Text width (618px) vs Txt Len (16 char?)

        //SyncPromise.resolve().always((action, data)->{
            ArrayList<HashMap<String,String>> S = DH.CursorSorter( DH.getReadableDatabase().query(DH.DBname,new String[]{DH.ID,DH.TITLE,DH.YMDHMS},null,null,null,null,null) );

            //System.out.println("====\n"+S+"\n====");

            for (HashMap<String,String> x : S) {
                String PureTitle = x.get(DH.TITLE), PureYMD = x.get(DH.YMDHMS), PureID = x.get(DH.ID);

                TableRow Tr = SetupRow(PureTitle);
                Tr.setTag(PureYMD + "-" + PureID);
                TL.addView(Tr);
            }

            DH.close();
            //action.resolve();
        //}).then((a,d)->{

            ((View)(SelAllTv).getParent()).setOnClickListener(v->{
                CheckBox cb = (CheckBox) ((ViewGroup)v).getChildAt(1);
                cb.setChecked( ! cb.isChecked() ); //Reverse check of checkbox

                DelFragTopButtClicked();
            } );

            //a.resolve();
        //}).start();
        DH.close();
    }

    private float DP2Pixel(float DP){
        //PixeltoDP //TypedValue.COMPLEX_UNIT_PX -- apply dim returns px always
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,DP,getResources().getDisplayMetrics());

        ////Use this for dimension convert PXtoDP DPtoPX
        //Density vs dpi --similar
        //float Dp = Pixel / getResources().getDisplayMetrics().density;
        //float Pixel = Dp * getResources().getDisplayMetrics().density;
    }

    private TableRow SetupRow(String Title){
        //Dupe / Clone not allowed!
        TextView Tv = new TextView(getContext()); CheckBox Cb = new CheckBox(getContext()); TableRow Tr = new TableRow(getContext()); ViewGroup.LayoutParams Params;

        //TextView
        Params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,0);
        //PadVal = (int)Math.floor(new Home2().DPtoPixel(3)); //Error to run new Activity class
        int PadVal = (int)Math.floor(DP2Pixel(8));
        Tv.setPadding(PadVal,PadVal,PadVal,PadVal);

        Tv.setSingleLine(true); Tv.setMaxLines(1);
        Tv.setGravity(Gravity.CENTER); Tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Tv.setTypeface(null, Typeface.BOLD);
        Tv.setLayoutParams(Params); Tv.setText(Title);

        //Setting up marquee
        Tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        Tv.setMarqueeRepeatLimit(9999); Tv.setSelected(true);
            //Seems to stay even if something else is selected

        Tv.setTextSize(TypedValue.COMPLEX_UNIT_PX , ((TextView)getActivity().findViewById(R.id.DelFragSelAll)).getTextSize() -10 );
        //TextViewCompat.setAutoSizeTextTypeWithDefaults(Tv,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);


        Tv.setBackgroundResource(R.drawable.roundborderdel); Tv.setTextColor( ((TextView)getActivity().findViewById(R.id.DelFragSelAll)).getCurrentTextColor() );

        //CheckBox
        Params = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT,1);
        Cb.setLayoutParams(Params); Cb.setClickable(false);

        //TableRow
        Tr.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Tr.addView(Tv,0);Tr.addView(Cb,1);

        //onclick listeners #2
        Tr.setOnClickListener( this::DelFragButtClicked );
        Tr.setOnLongClickListener((v)->{
            TextView tv = (TextView) ((ViewGroup) v).getChildAt(0);
            Toast.makeText(getContext(), ""+ (tv).getText(), Toast.LENGTH_LONG).show();
            return true;
        });

        return Tr;
    }

    private void DelFragTopButtClicked(){
        TableLayout TL = getActivity().findViewById(R.id.DelFragTable);
        for(int i=1;i<TL.getChildCount();i++){
            DelFragButtClicked( TL.getChildAt(i) );
        }
    }

    private void DelFragButtClicked(View v){
        //default set background to drawable.. on click make whole bg same col.. change txt from brown to yellow
        TableRow tr = (TableRow) v;
        int TextCol=0;
        CheckBox cb = (CheckBox) tr.getChildAt(1);
        cb.setChecked( !cb.isChecked() );

        TypedArray ta = this.getActivity().obtainStyledAttributes(new int[]{R.attr.Title,R.attr.Interactable,R.attr.Text});

        if (cb.isChecked()){
            TextCol= ta.getColor(0,-1); //title
            tr.getChildAt(0).setBackgroundColor(ta.getColor(1,-1)); //Interactable
        }else{
            TextCol=ta.getColor(2,-1); //text
            tr.getChildAt(0).setBackgroundResource(R.drawable.roundborderdel);
        }

        ta.recycle();

        ((TextView)tr.getChildAt(0)).setTextColor(TextCol);
    }

    private void DelFragDelBut(View v){
        TextView tv = (TextView) v;

        String tv2 = tv.getText() +"";
        tv.setText("DELETED!");
        tv.setBackgroundColor(getResources().getColor(R.color.GreenTick));

        new Handler().postDelayed(() -> {
            tv.setText(tv2);
            tv.setBackgroundResource(R.drawable.roundbordernote);
        },1300);

        //Check all checked => del them from DB - exclude top
        TableLayout TL = requireActivity().findViewById(R.id.DelFragTable);
        ArrayList<ArrayList<String>> ToBeDel = new ArrayList<>();
        ArrayList<TableRow> ToBeRemoved = new ArrayList<>();
        for(int i=1;i<TL.getChildCount();i++){
            TableRow TR = (TableRow) TL.getChildAt(i);
            if( ((CheckBox) TR.getChildAt(1)).isChecked() ){
                ToBeRemoved.add(TR); String Title,YMD,ID;

                Title = ""+((TextView)TR.getChildAt(0)).getText();
                YMD = TR.getTag().toString().split("-")[0];
                ID = TR.getTag().toString().split("-")[1];
                ToBeDel.add(new ArrayList<>(Arrays.asList(ID,Title,YMD)));
            }
        }
        //System.out.println( ToBeDel );

        if(ToBeDel.size() > 0){
            QuickDelFormat(ToBeDel);

            //Removed views - auto move existing views up
            for(TableRow tr : ToBeRemoved){ TL.removeView(tr); }

        }else{
            Toast.makeText(getContext(), "Nothing selected to delete!", Toast.LENGTH_SHORT).show();
        }
    }

    private void QuickDelFormat(ArrayList<ArrayList<String>> ToBeDel) {
        //ID|Title|YMD
        final DatabaseHandler DH = new DatabaseHandler(getContext());
        ArrayList<String> Args=new ArrayList<>();
        String Query = "";

        for (ArrayList<String> IdTitleYmd : ToBeDel) {
            //ID,TITLE,YMD
            Query +=
                    String.format(
                            "( %1$s = %2$s AND %3$s = %4$s AND %5$s = %6$s )",
                            DH.ID, "?", DH.TITLE, "?", DH.YMDHMS, "?"
                    );
            Args.add(IdTitleYmd.get(0));Args.add(IdTitleYmd.get(1));Args.add(IdTitleYmd.get(2));
            if (IdTitleYmd == ToBeDel.get(ToBeDel.size() - 1)) {
                Query += "";
            } else {
                Query += " OR ";
            }
        }
        //System.out.println(Query);
        Toast.makeText(getContext(), "DELETED SELECTED!", Toast.LENGTH_SHORT).show();

        //Has to stay as delete returns num of rows deleted as it deletes them
        System.out.println("Num of rows deleted: "+
        DH.getWritableDatabase().delete(DH.DBname,Query,
                Args.toArray(new String[]{}))
        );
        DH.close();

        for (ArrayList<String> IdTitleYmd : ToBeDel) {
            int ID = Integer.parseInt(IdTitleYmd.get(0)) +1;
            //System.out.println("Remove worker: id "+ ID);
            new NotiActionHandler().onReceive(getContext(),new Intent(getContext().getApplicationContext(), DeleteFragment.class).putExtra("Code","CANCEL").putExtra("LinkageID",ID).putExtra("D1","RemindFulNoti"));
        }
    }
}
