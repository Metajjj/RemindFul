package com.example.remindful;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.frag_in, R.anim.frag_out)
                    .remove(DatepickerFragment.this).commit();
        });
        getActivity().findViewById(R.id.PickerFragMenu).setOnClickListener(null);

        DatePicker DP = new DatePicker(context, null); //no 3rd style makes it rotating, prbolem with squashed
        DP.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //getActivity().findViewById(R.id.PickerFragMenu).setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));

        DP.init(0,0,0,(dp,Year,Month,Day)->{
            //System.out.println(MessageFormat.format("Y:{0}|M:{1}|D:{2}", Year,Month+1,Day));
            ((TextView)getActivity().findViewById(R.id.RemFragDateYear)).setText(Year+"");
            ((TextView)getActivity().findViewById(R.id.RemFragDateMonth)).setText((Month+1)+"");
            ((TextView)getActivity().findViewById(R.id.RemFragDateDay)).setText(Day+"");
        });

        DP.setMinDate(Calendar.getInstance().getTimeInMillis());

        //Picking apart DatePicker widget to customise it
         ///ChildViewFinder(DP);
        //DP -> LinLayouts -> 2x TxtVw (top/heading)
        //DP -> dayPickerView (background of calender) -> ImgButtons (left and right arrows) & DayPickerViewPager (??)

        ((ViewGroup)getActivity().findViewById(R.id.PickerFragMenu)).addView(DP);

        ChildViewFinder(DP);

        /*
        CalendarView cv = new CalendarView(context);
        cv.setBackgroundColor(R.attr.Title); cv.setMinDate(Calendar.getInstance().getTimeInMillis()); cv.setWeekSeparatorLineColor(R.attr.Interactable);
        System.out.println("Date txt: "+
            cv.getDateTextAppearance()
        );



        TypedArray ta = getActivity().obtainStyledAttributes(new int[]{R.attr.Title,R.attr.Interactable, R.attr.Text});
        //getResources().getResourceName(R.style.CalViewText)

        //cv.setDateTextAppearance(R.style.CalViewText);
        //((TextView)((View)cv)).setTextColor(getResources().getColor(R.color.GreenTick));
        //classcast err

         //https://stackoverflow.com/questions/14980242/small-numbers-in-calendarview-android/36321828#36321828

        ((ViewGroup)getActivity().findViewById(R.id.PickerFragMenu)).addView(cv);

        cv.setOnDateChangeListener((Cv,Year,Month,Day)-> {
            System.out.println(MessageFormat.format("Y:{0}|M:{1}|D:{2}", Year+"", Month + 1, Day));
            ((TextView) getActivity().findViewById(R.id.RemFragDateYear)).setText(Year + "");
            ((TextView) getActivity().findViewById(R.id.RemFragDateMonth)).setText((Month + 1) + "");
            ((TextView) getActivity().findViewById(R.id.RemFragDateDay)).setText(Day + "");
        });
        ChildViewFinder(cv);*/
    }

    private void ChildViewFinder(ViewGroup vg){
        for (int i = 0; i<vg.getChildCount(); i++){
            try{
                //System.out.println(vg.getClass().getName() +" -> "+ vg.getChildAt(i).getClass().getName());

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
                        //vg.setBackgroundColor( ta.getColor(0,-1) );
                        (getActivity().findViewById(R.id.PickerFragMenu)).setBackgroundColor(ta.getColor(0,1));
                        int MyPad = (int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,5,getResources().getDisplayMetrics()));
                        //vg.setPadding(MyPad,MyPad,MyPad,MyPad);
                        vg.setBackgroundColor(Color.parseColor("#DDDDDD"));

                        ta.recycle();
                    }/*
                    else if (vg.getChildAt(i) instanceof android.widget.DayPickerView){
                        System.out.println("TV: "+((TextView)vg.getChildAt(i)).getText());
                    }*/
                }

            } catch (Exception e){
                System.err.println("ERR: "+e);
            }
        }
    }
}
