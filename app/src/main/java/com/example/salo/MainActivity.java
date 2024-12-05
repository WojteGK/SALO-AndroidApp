package com.example.salo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.Manifest;


public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private static final int REQUEST_CAMERA_PERMISSION = 100;


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
        requestCameraPermission();
        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Initialize Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add Hamburger Icon
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, // Add this in strings.xml
                R.string.navigation_drawer_close // Add this in strings.xml
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();




        // Handle Navigation View Item Clicks
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Toast.makeText(MainActivity.this, "Home clicked", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_profile) {
                    Toast.makeText(MainActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Unknown item selected", Toast.LENGTH_SHORT).show();
                }

                drawerLayout.closeDrawers(); // Close drawer after handling
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Close drawer if open, otherwise perform default back action
        if (drawerLayout.isDrawerOpen(findViewById(R.id.navigation_view))) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void requestCameraPermission() {
        // Check if the permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
            );
        } else {
            // Permission already granted
            Toast.makeText(this, "Camera permission already granted", Toast.LENGTH_SHORT).show();
        }
    }
}