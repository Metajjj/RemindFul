package com.example.remindful;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MenuFragment extends DialogFragment {

    private Context context;

    @Override @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        context = getContext().getApplicationContext();

        //TransitionInflater TI = TransitionInflater.from(context); setEnterTransition(TI.inflateTransition(R.transition.transition)); setExitTransition(TI.inflateTransition(R.anim.frag_out));
        //Animations give more control than transitions

        return inflater.inflate(R.layout.menu_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //onClick FUNCTIONS

        //Remove fragment and updates activity
        getActivity().findViewById(R.id.MenuFragBg).setOnClickListener(v-> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.frag_in, R.anim.frag_out)
                    .remove(MenuFragment.this).commit();
            startActivity(new Intent(context, Home2.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
        } );

        getActivity().findViewById(R.id.MenuFragMenu).setOnClickListener(null);

        //Creates new note
        getActivity().findViewById(R.id.MenuFragNewNote).setOnClickListener(v-> {

            getActivity().findViewById(R.id.MenuFragBg).performClick();

            startActivity(new Intent(context,NewNote.class));
        } );

        //Opens del frag
        getActivity().findViewById(R.id.MenuFragWipeSel).setOnClickListener(v-> {
            Toast.makeText(context,"Del time!",Toast.LENGTH_LONG).show();

            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.frag_in, R.anim.frag_out)
                    .replace(R.id.home2FragHolder,DeleteFragment.class,null).commit();

            getActivity().findViewById(R.id.home2FragHolder).bringToFront();
        } );

        //Del all notes
        getActivity().findViewById(R.id.MenuFragWipeAll).setOnClickListener(v->
        {
            DatabaseHandler DH = new DatabaseHandler(context); DH.ResetTable(); DH.close();
            getActivity().findViewById(R.id.MenuFragBg).performClick();
        });

        //BGM
        getActivity().findViewById(R.id.MenuFragMusic).setOnClickListener(v->{
            getActivity().findViewById(R.id.MenuFragBg).performClick();

            startActivity(new Intent(context, BGM.class));
        });
    }
}
