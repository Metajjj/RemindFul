package com.example.remindful;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class RemindFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        MainContainer = container;
        //new Home().WriteLine(""+container.getClass().getName() ); //FRAMELAYOUT

        return inflater.inflate(R.layout.delete_fragment, container, false);
    }
    private ViewGroup MainContainer;

    @Override
    public void onStart() {
        super.onStart();
        final DatabaseHandler DH = new DatabaseHandler(getContext());
        MainContainer.findViewById(R.id.RemFragBg).setOnClickListener(this::CloseFrag);


    }

    public void CloseFrag(View v){
        //null ptr except - activity
        //new Home2().Switchy( MainContainer.findViewById(R.id.home2ViewStyle) );

        getParentFragmentManager().beginTransaction().remove(RemindFragment.this).commit();
        //getActivity().findViewById(R.id.home2FragHolder).back
        //startActivity(new Intent(getContext(),Home2.class));
    }
}
