package com.example.myhealth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button btnWorkout, btnMealPlanner, btnLogout, btnProfile;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ Firebase App Init Check
        FirebaseApp.initializeApp(this);
        Log.d("FIREBASE_CHECK", "Firebase initialized: " + FirebaseApp.getInstance().getName());

        auth = FirebaseAuth.getInstance();

        // 🔘 Button declarations
        btnWorkout = findViewById(R.id.btnWorkout);
        btnMealPlanner = findViewById(R.id.btnMealPlanner);
        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);

        // ▶ Workout Tracker
        btnWorkout.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, WorkoutTrackerActivity.class));
            Toast.makeText(this, "Opening Workout Tracker...", Toast.LENGTH_SHORT).show();
        });

        // ▶ Meal Planner
        btnMealPlanner.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MealPlannerActivity.class));
            Toast.makeText(this, "Opening Meal Planner...", Toast.LENGTH_SHORT).show();
        });

        // ▶ Profile
        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            Toast.makeText(this, "Opening Profile...", Toast.LENGTH_SHORT).show();
        });

        // 🔓 Logout
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish(); // 🔐 Prevent back-navigation to main screen
        });
    }
}


