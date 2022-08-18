package com.vaslim.autopilot.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.vaslim.autopilot.MainActivity;
import com.vaslim.autopilot.R;
import com.vaslim.autopilot.Turn;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManualFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManualFragment extends Fragment {



    public ManualFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ManualFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ManualFragment newInstance(String param1, String param2) {
        ManualFragment fragment = new ManualFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manual, container, false);
        Button buttonLeft = view.findViewById(R.id.button_left);
        Button buttonRight = view.findViewById(R.id.button_right);
        buttonLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN: {sendToController(Turn.CHAR_TURN_LEFT); return true;}
                    case MotionEvent.ACTION_UP: {sendToController(Turn.CHAR_TURN_STOP); return true;}
                }
                return false;
            }
        });
        buttonRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN: {sendToController(Turn.CHAR_TURN_RIGHT); return true;}
                    case MotionEvent.ACTION_UP: {sendToController(Turn.CHAR_TURN_STOP); return true;}
                }
                return false;
            }
        });

        return view;
    }

    private void sendToController(char command) {
        System.out.println("COMMAND "+command);
        MainActivity.ardutooth.sendChar(command);
    }
}