package com.vaslim.autopilot.fragments.compass;

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

import com.vaslim.autopilot.MainActivity;
import com.vaslim.autopilot.R;
import com.vaslim.autopilot.SharedData;
import com.vaslim.autopilot.compass.Compass;
import com.vaslim.autopilot.compass.SOTWFormatter;
import com.vaslim.autopilot.ruddercontrol.RudderControlThread;


public abstract class CompassFragmentAbstract extends Fragment {
    private static final String TAG = "CompassFragment";

    private Compass compass;
    private ImageView arrowView;
    private TextView sotwLabel;  // SOTW is for "side of the world"

    private float currentAzimuth;
    private SOTWFormatter sotwFormatter;




    public CompassFragmentAbstract() {
        // Required empty public constructor
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
        if(MainActivity.rudderControlRunnable != null){
            MainActivity.rudderControlRunnable.shutdown();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        compass.start();
        if(MainActivity.rudderControlRunnable != null){
            MainActivity.rudderControlRunnable.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "stop compass");
        compass.stop();
        if(MainActivity.rudderControlRunnable != null){
            MainActivity.rudderControlRunnable.shutdown();
        }
    }

    private void setupCompass() {
        compass = new Compass(CompassFragmentAbstract.this.getActivity());
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
                if(SharedData.mode == SharedData.Mode.COMPASS){
                    SharedData.currentBearing = azimuth;
                    //System.out.println("NEW AZIMUTH "+SharedData.currentBearing);
                }

                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966
                CompassFragmentAbstract.this.getActivity().runOnUiThread(new Runnable() {
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
        sotwFormatter = new SOTWFormatter(CompassFragmentAbstract.this.getActivity());
        arrowView = view.findViewById(R.id.main_image_hands);
        System.out.println("HELLO: "+ CompassFragmentAbstract.this.getActivity().findViewById(R.id.main_image_hands));
        sotwLabel = view.findViewById(R.id.sotw_label);
        setupCompass();
        Button buttonApply = view.findViewById(R.id.button_apply_compass);
        buttonApply.setOnClickListener(view1 -> updateTargetBearing());

        Button buttonApplySensitivity = view.findViewById(R.id.button_apply_sensitivity_compass);
        buttonApplySensitivity.setOnClickListener(view1 -> updateSensitivity());
        return view;
    }

    private void updateSensitivity() {
        EditText editTextSensitivity = CompassFragmentAbstract.this.getActivity().findViewById(R.id.edit_sensitivity_compass);
        int value = Integer.parseInt(editTextSensitivity.getText().toString());
        if(value>=1 && value<=10){
            SharedData.sensitivity = value;
        }
        else{
            showToast("Value must be 1-10");
        }
    }

    private void updateTargetBearing() {
        EditText editTextTargetBearing = CompassFragmentAbstract.this.getActivity().findViewById(R.id.edit_target_bearing_compass);
        double targetBearing = Double.parseDouble(editTextTargetBearing.getText().toString());
        if(targetBearing>=0 && targetBearing <=359){
            SharedData.targetBearing = targetBearing;
            MainActivity.rudderControlRunnable = chooseThreadAlgorithm();
            MainActivity.rudderControlRunnable.start();
        }
        else{
            showToast("Target bearing must be 0-359");
        }
    }

    protected abstract RudderControlThread chooseThreadAlgorithm();

    private void showToast(String message){
        Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}