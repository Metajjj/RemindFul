package com.example.remindful;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


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
            getParentFragmentManager().beginTransaction().remove(TimepickerFragment.this).commit();
        });
        getActivity().findViewById(R.id.PickerFragMenu).setOnClickListener(null);

        TimePicker TP = new TimePicker(context);
        TP.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((ViewGroup)getActivity().findViewById(R.id.PickerFragMenu)).addView(TP);


    }
}
