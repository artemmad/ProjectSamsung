package com.example.tasktrack.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tasktrack.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//
//
//        getFragmentManager().beginTransaction()
//                .replace(R.id.frame, new SettingsFragment(), "pref")
//                .commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
