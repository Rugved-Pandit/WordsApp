package com.example.wordsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView info_text = findViewById(R.id.info_text);
        info_text.setTextIsSelectable(true);
        info_text.setMovementMethod(new ScrollingMovementMethod());
        info_text.setText(getString(R.string.info));
    }
}