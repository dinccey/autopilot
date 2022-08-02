package com.vaslim.autopilot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vaslim.autopilot.fragments.AutopilotFragment;
import com.vaslim.autopilot.fragments.ManualFragment;
import com.vaslim.autopilot.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    AutopilotFragment autopilotFragment = new AutopilotFragment();
    ManualFragment manualFragment = new ManualFragment();
    SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,autopilotFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_autopilot: {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,autopilotFragment).commit();
                        return true;
                    }
                    case R.id.menu_manual: {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,manualFragment).commit();
                        return true;
                    }
                    case R.id.menu_settings: {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,settingsFragment).commit();
                        return true;
                    }
                }

                return false;
            }
        });
    }
}