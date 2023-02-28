package com.example.remindful;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;


public class TimepickerFragment extends DialogFragment {

    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        context = getContext().getApplicationContext();

        return inflater.inflate(R.layout.picker_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().findViewById(R.id.PickerFragBg).setOnClickListener(v-> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.frag_in, R.anim.frag_out)
                    .remove(TimepickerFragment.this).commit();
        });
        getActivity().findViewById(R.id.PickerFragMenu).setOnClickListener(null);

        TimePicker TP = new TimePicker(context,null); //no style 3rd makes it spinner
        TP.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((ViewGroup)getActivity().findViewById(R.id.PickerFragMenu)).addView(TP);

        //Styling TimePicker
        TP.setIs24HourView(true);
        TP.setPaddingRelative(0,0,0,
                (int) Math.floor(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10,context.getResources().getDisplayMetrics()))
        );

        //set init time
        ((TextView)getActivity().findViewById(R.id.RemFragTimeSec)).setText("00");
        ((TextView)getActivity().findViewById(R.id.RemFragTimeMin)).setText(Calendar.getInstance().get(Calendar.MINUTE) +"");
        ((TextView)getActivity().findViewById(R.id.RemFragTimeHour)).setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+"");

        TP.setOnTimeChangedListener((timePicker, Hour, Min) -> {
            //System.out.println("H:"+Hour+" M:"+Min);
            ((TextView)getActivity().findViewById(R.id.RemFragTimeSec)).setText("00");
            ((TextView)getActivity().findViewById(R.id.RemFragTimeMin)).setText(Min+"");
            ((TextView)getActivity().findViewById(R.id.RemFragTimeHour)).setText(Hour+"");

            ChildViewFinder(timePicker);
        });

        TypedArray ta = getActivity().obtainStyledAttributes(new int[]{R.attr.Title,R.attr.Interactable});
        TP.setBackgroundColor(ta.getColor(0,-1)); ta.recycle();
        //Everything is top heading part, buttons and stuff is RadialTimePickerView
         ChildViewFinder(TP);
        //TP -> LinLayout -> RelLayout -> 2x NumericTxtVw & TxtVw (Hour Min & :)
        //LinLayout -> RadioGroup -> RadioButtons x2 & RadialTimePickerView (AM PM & ??)
    }

    private void ChildViewFinder(ViewGroup vg){
        for (int i = 0; i<vg.getChildCount(); i++){
            try{
                //System.out.println(vg.getClass().getName() +" -> "+ vg.getChildAt(i).getClass().getName());

                try {
                    ChildViewFinder((ViewGroup) vg.getChildAt(i));
                }catch (ClassCastException e) {
                    //System.out.println("\t\t\t\t\t\t\t\tis child!");

                    if (vg.getChildAt(i) instanceof TextView) {
                        TextView v = (TextView) vg.getChildAt(i);
                        //System.out.println( v.getText() ); //TxtVw is top part
                        TypedArray ta = getActivity().obtainStyledAttributes(new int[]{R.attr.Title, R.attr.Interactable});
                        v.setTextColor(ta.getColor(0, -1));
                        vg.setBackgroundColor(ta.getColor(1, -1));

                        ta.recycle();
                    }/*
                    else if (vg.getChildAt(i) instanceof android.widget.RadialTimePickerView){
                        System.out.println("TV: "+((TextView)vg.getChildAt(i)).getText());
                    }*/
                }

            } catch (Exception e){
                System.err.println("ERR: "+e);
            }
        }
    }
}
