package com.vaslim.autopilot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vaslim.autopilot.fragments.GPSFragment;
import com.vaslim.autopilot.fragments.compass.CompassFragment1;
import com.vaslim.autopilot.fragments.compass.CompassFragment2;
import com.vaslim.autopilot.fragments.compass.CompassFragmentAbstract;
import com.vaslim.autopilot.fragments.ManualFragment;
import com.vaslim.autopilot.fragments.SettingsFragment;
import com.vaslim.autopilot.ruddercontrol.RudderControlThread;

import io.github.giuseppebrb.ardutooth.Ardutooth;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;

    public static RudderControlThread rudderControlRunnable = null;
    public static Ardutooth ardutooth;
    GPSFragment gpsFragment = new GPSFragment();
    ManualFragment manualFragment = new ManualFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    CompassFragmentAbstract compassFragment1 = new CompassFragment1();
    CompassFragmentAbstract compassFragment2 = new CompassFragment2();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.WAKE_LOCK},99);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        acquireWakelock();

        ardutooth = Ardutooth.getInstance(this);
        ardutooth.setConnection();



        bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, compassFragment1).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_autopilot_2: {
                        SharedData.mode = SharedData.Mode.COMPASS;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,compassFragment1).commit();
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
                    case R.id.menu_autopilot_1:{
                        SharedData.mode = SharedData.Mode.COMPASS;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,compassFragment2).commit();
                        return true;
                    }
                }

                return false;
            }
        });


    }

    private void acquireWakelock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "AutopilotApp::RuntimeWakelock");
        wakeLock.acquire();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

}