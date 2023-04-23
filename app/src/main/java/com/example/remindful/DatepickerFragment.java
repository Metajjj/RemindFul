package com.example.remindful;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.MessageFormat;
import java.util.Calendar;


public class DatepickerFragment extends DialogFragment {

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
            getParentFragmentManager().beginTransaction().remove(DatepickerFragment.this).commit();
        });
        getActivity().findViewById(R.id.PickerFragMenu).setOnClickListener(null);

        DatePicker DP = new DatePicker(context);
        DP.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((ViewGroup)getActivity().findViewById(R.id.PickerFragMenu)).addView(DP);

        DP.init(0,0,0,(dp,Year,Month,Day)->{
            System.out.println(MessageFormat.format("Y:{0}|M:{1}|D:{2}", Year,Month+1,Day));
            ((TextView)getActivity().findViewById(R.id.RemFragDateYear)).setText(Year+"");
            ((TextView)getActivity().findViewById(R.id.RemFragDateMonth)).setText((Month+1)+"");
            ((TextView)getActivity().findViewById(R.id.RemFragDateDay)).setText(Day+"");
        });

        DP.setMinDate(Calendar.getInstance().getTimeInMillis());

        //Picking apart DatePicker widget to customise it
        ChildViewFinder(DP);
        //DP -> LinLayouts -> 2x TxtVw (top/heading)
        //DP -> dayPickerView (background of calender) -> ImgButtons (left and right arrows) & DayPickerViewPager (??)
    }

    private void ChildViewFinder(ViewGroup vg){
        int j=0;
        for (int i = 0; i<vg.getChildCount(); i++){
            try{
                System.out.println(vg.getClass().getName() +" -> "+ vg.getChildAt(i).getClass().getName());

                try {
                    ChildViewFinder((ViewGroup) vg.getChildAt(i));
                }catch (ClassCastException e) {
                    //System.out.println("\t\t\t\t\t\t\t\tis child!");
                    if(vg.getChildAt(i) instanceof TextView) {
                        TextView v = (TextView) vg.getChildAt(i);
                        //System.out.println( v.getText() ); TxtVw is top part
                        TypedArray ta = getActivity().obtainStyledAttributes(new int[]{R.attr.Title,R.attr.Interactable});
                        v.setTextColor(ta.getColor(0,-1));
                        vg.setBackgroundColor(ta.getColor(1,-1));

                        ta.recycle();
                    } else if (vg.getChildAt(i) instanceof ImageButton) {
                        ImageButton v = (ImageButton) vg.getChildAt(i);

                        TypedArray ta = getActivity().obtainStyledAttributes(new int[]{R.attr.Title,R.attr.Interactable});

                        v.setBackgroundColor(ta.getColor(1,-1));
                        vg.setBackgroundColor(ta.getColor(0,-1));

                        ta.recycle();
                    }
                    else if (vg.getChildAt(i) instanceof android.widget.TextView){
                        System.out.println("TV: "+((TextView)vg.getChildAt(i)).getText());
                    }
                }

            } catch (Exception e){
                System.err.println("ERR: "+e);

            }
        }
    }
}
