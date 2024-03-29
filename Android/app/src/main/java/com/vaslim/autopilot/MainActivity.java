package com.vaslim.autopilot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vaslim.autopilot.fragments.AutopilotFragment;
import com.vaslim.autopilot.fragments.CompassFragment;
import com.vaslim.autopilot.fragments.ManualFragment;
import com.vaslim.autopilot.fragments.SettingsFragment;

import io.github.giuseppebrb.ardutooth.Ardutooth;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    public static Ardutooth ardutooth;
    AutopilotFragment autopilotFragment = new AutopilotFragment();
    ManualFragment manualFragment = new ManualFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    CompassFragment compassFragment = new CompassFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},99);
        ardutooth = Ardutooth.getInstance(this);
        ardutooth.setConnection();

        //start autopilot thread
        AutopilotThread autopilotThread = new AutopilotThread();
        autopilotThread.start();


        bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,autopilotFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_autopilot: {
                        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                            SharedData.mode = SharedData.Mode.GPS;
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,autopilotFragment).commit();
                        }else{
                            Toast.makeText(MainActivity.this, "No location permission", Toast.LENGTH_SHORT).show();
                        }

                        return true;
                    }
                    case R.id.menu_manual: {
                        SharedData.mode = SharedData.Mode.MANUAL;
                        SharedData.targetBearing = -1;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,manualFragment).commit();
                        return true;
                    }
                    case R.id.menu_settings: {
                        SharedData.targetBearing = -1;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,settingsFragment).commit();
                        return true;
                    }
                    case R.id.menu_compass:{
                        SharedData.mode = SharedData.Mode.COMPASS;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,compassFragment).commit();
                        return true;
                    }
                }

                return false;
            }
        });


    }

}