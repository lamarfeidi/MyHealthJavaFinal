package com.example.myhealth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class EmailAuthActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText editEmail, editPassword;
    private Button btnEmailSignIn, btnEmailRegister, btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_auth);

        auth = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnEmailSignIn = findViewById(R.id.btnEmailSignIn);
        btnEmailRegister = findViewById(R.id.btnEmailRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnBackToLogin.setOnClickListener(v -> finish());

        btnEmailRegister.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String pass  = editPassword.getText().toString().trim();
            if (!isValid(email, pass)) return;

            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(result -> {
                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                        goHome();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EmailAuth", "Register failed", e);
                        Toast.makeText(this, "Register failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        btnEmailSignIn.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String pass  = editPassword.getText().toString().trim();
            if (!isValid(email, pass)) return;

            auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(result -> goHome())
                    .addOnFailureListener(e -> {
                        Log.e("EmailAuth", "Sign-in failed", e);
                        Toast.makeText(this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    private boolean isValid(String email, String pass) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void goHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

