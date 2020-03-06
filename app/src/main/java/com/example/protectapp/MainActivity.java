package com.example.protectapp;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File data = getDir("data", MODE_PRIVATE);
        File apkFile = new File(getApplicationInfo().sourceDir);
        Log.i("hello", "xxx");
    }
}
