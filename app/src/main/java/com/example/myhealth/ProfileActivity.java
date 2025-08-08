package com.example.myhealth;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName, txtEmail;
    private ImageView profileImageView;
    private Button btnLogout, btnEditProfile; // Corrected to btnEditProfile

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase components
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize UI elements
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        profileImageView = findViewById(R.id.profileImageView);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btn_edit_profile);  // Reference the correct ID here

        // Display current user info
        if (currentUser != null) {
            txtName.setText(currentUser.getDisplayName());
            txtEmail.setText(currentUser.getEmail());

            // Load profile picture if exists
            if (currentUser.getPhotoUrl() != null) {
                Picasso.get().load(currentUser.getPhotoUrl()).into(profileImageView);
            }
        }

        // Logout functionality
        btnLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            finish();  // Close activity and return to login screen
        });

        // Edit profile button functionality
        btnEditProfile.setOnClickListener(v -> {
            // You can add an animation or logic to edit the profile
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            v.startAnimation(fadeIn);
            // Add your edit profile logic here, for example:
            Toast.makeText(this, "Edit Profile functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
    }
}

