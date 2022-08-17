package com.vaslim.autopilot.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vaslim.autopilot.R;
import com.vaslim.autopilot.SharedData;
import com.vaslim.autopilot.compass.Compass;
import com.vaslim.autopilot.compass.SOTWFormatter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CompassFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompassFragment extends Fragment {
    private static final String TAG = "CompassFragment";

    private Compass compass;
    private ImageView arrowView;
    private TextView sotwLabel;  // SOTW is for "side of the world"

    private float currentAzimuth;
    private SOTWFormatter sotwFormatter;




    public CompassFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CompassFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CompassFragment newInstance(String param1, String param2) {
        CompassFragment fragment = new CompassFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "start compass");
        compass.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        compass.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        compass.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "stop compass");
        compass.stop();
    }

    private void setupCompass() {
        compass = new Compass(CompassFragment.this.getActivity());
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
    }

    private void adjustArrow(float azimuth) {
        /*Log.d(TAG, "will set rotation from " + currentAzimuth + " to "
                + azimuth);*/

        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = azimuth;

        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        arrowView.startAnimation(an);
    }

    private void adjustSotwLabel(float azimuth) {
        sotwLabel.setText(sotwFormatter.format(azimuth));
    }

    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
                SharedData.currentBearing = azimuth;
                System.out.println("NEW AZIMUTH "+azimuth);
                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966
                CompassFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adjustArrow(azimuth);
                        adjustSotwLabel(azimuth);
                    }
                });
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_compass, container, false);
        sotwFormatter = new SOTWFormatter(CompassFragment.this.getActivity());
        arrowView = view.findViewById(R.id.main_image_hands);
        System.out.println("HELLO: "+CompassFragment.this.getActivity().findViewById(R.id.main_image_hands));
        sotwLabel = view.findViewById(R.id.sotw_label);
        setupCompass();
        Button buttonApply = view.findViewById(R.id.button_apply_compass);
        buttonApply.setOnClickListener(view1 -> updateTargetBearing());

        Button buttonApplySensitivity = view.findViewById(R.id.button_apply_sensitivity_compass);
        buttonApplySensitivity.setOnClickListener(view1 -> updateSensitivity());
        return view;
    }

    private void updateSensitivity() {
        EditText editTextSensitivity = CompassFragment.this.getActivity().findViewById(R.id.edit_sensitivity_compass);
        int value = Integer.parseInt(editTextSensitivity.getText().toString());
        if(value>=1 && value<=10){
            SharedData.sensitivity = value;
        }
        else{
            showToast("Value must be 1-10");
        }
    }

    private void updateTargetBearing() {
        EditText editTextTargetBearing = CompassFragment.this.getActivity().findViewById(R.id.edit_target_bearing_compass);
        double targetBearing = Double.parseDouble(editTextTargetBearing.getText().toString());
        if(targetBearing>=0 && targetBearing <=359){
            SharedData.targetBearing = targetBearing;
        }
        else{
            showToast("Target bearing must be 0-359");
        }
    }
    private void showToast(String message){
        Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}