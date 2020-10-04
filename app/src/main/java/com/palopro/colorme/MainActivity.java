package com.palopro.colorme;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import ai.fritz.core.Fritz;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "d9b274bddd4d41ff970bc00c449de18d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fritz.configure(this, API_KEY);

        setContentView(R.layout.activity_main);
    }
}