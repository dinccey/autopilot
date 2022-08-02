package com.vaslim.autopilot.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.vaslim.autopilot.R;


public class AutopilotFragment extends Fragment {

    public static final int DEFAULT_UPDATE_INTERVAL = 100;
    public static final int FAST_UPDATE_INTERVAL = 50;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    TextView tvGPSBearing, tvGPSAccuracy;

    public AutopilotFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateViews(locationResult.getLastLocation());
            }
        };

        updateGPS();
        locationUpdates();

    }

    private void locationUpdates() {
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_autopilot, container, false);

        return view;
    }


    private void updateGPS(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        if(ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this.getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateViews(location);
                }
            });
        }
        else{
            this.getActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},99);
        }
    }

    private void updateViews(Location location){
        tvGPSBearing = this.getActivity().findViewById(R.id.tv_gps_bearing);
        tvGPSAccuracy = this.getActivity().findViewById(R.id.tv_gps_accuracy);
        //float bearing = location.getBearing();
        //System.out.println("ACCURACY"+location.getAccuracy());
        tvGPSBearing.setText(String.valueOf(location.getBearing()));
        tvGPSAccuracy.setText(String.valueOf(location.getAccuracy()) + " "+location.getLatitude()+";"+location.getLongitude());
    }
}