package com.example.salo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonPage1 = findViewById(R.id.buttonPage1);
        Button buttonPage2 = findViewById(R.id.buttonPage2);
        Button buttonPage3 = findViewById(R.id.buttonPage3);

        buttonPage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Camera.class));
            }
        });

    }
}