package com.example.remindful;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
        TableLayout TL = getActivity().findViewById(R.id.DelFragTable);
        for(int i=0;i<TL.getChildCount();i++){
            TL.getChildAt(i).setOnClickListener(this::DelFragButtClicked);

            //TableRow TR = (TableRow) TL.getChildAt(i);
            /*for(int j=0;j<TR.getChildCount();j++){
                TR.getChildAt(j).setOnClickListener(this::DelFragButtClicked);
            }*/
        }
    }

    public void DelFragButtClicked(View v){
        //default set background to drawable.. on click make whole bg same col.. change txt from brown to yellow

        //chvck box then ...
        ((ViewGroup)v).getChildAt(0).setBackgroundColor(Color.parseColor("#660000"));
        ((TextView)((ViewGroup)v).getChildAt(0)).setTextColor(Color.parseColor("#f4d882"));

        v.setBackgroundResource(R.drawable.roundborderdel);
    }
}
