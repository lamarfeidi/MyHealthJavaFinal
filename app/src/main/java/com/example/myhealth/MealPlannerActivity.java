package com.example.myhealth;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MealPlannerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MealAdapter adapter;
    private final List<Meal> mealList = new ArrayList<>();

    private EditText editMealName;
    private EditText editMealCalories;
    private Button btnAddMeal;
    private Button btnAddMealPhoto;

    private Uri pendingPhotoUri;
    private String lastUploadedPhotoUrl;

    // Ask for camera permission
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    takeMealPhotoInternal();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    // Launch the camera to write an image to our URI
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && pendingPhotoUri != null) {
                    uploadPhotoToStorage(pendingPhotoUri);
                } else {
                    Toast.makeText(this, "Photo canceled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planner);

        recyclerView = findViewById(R.id.mealRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        editMealName = findViewById(R.id.editMealName);
        editMealCalories = findViewById(R.id.editMealCalories);
        btnAddMeal = findViewById(R.id.btnAddMeal);
        btnAddMealPhoto = findViewById(R.id.btnAddMealPhoto);

        btnAddMealPhoto.setOnClickListener(v -> takeMealPhoto());
        btnAddMeal.setOnClickListener(v -> saveMealToFirestore());

        fetchMealsFromFirestore();
    }

    private void takeMealPhoto() {
        // Check runtime permission first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            takeMealPhotoInternal();
        }
    }

    private void takeMealPhotoInternal() {
        try {
            File imagesDir = new File(getCacheDir(), "images");
            if (!imagesDir.exists() && !imagesDir.mkdirs()) {
                Toast.makeText(this, "Cannot create image cache dir", Toast.LENGTH_SHORT).show();
                return;
            }
            File photo = File.createTempFile("meal_", ".jpg", imagesDir);
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photo
            );
            pendingPhotoUri = uri;
            cameraLauncher.launch(uri);
        } catch (Exception e) {
            Log.e("MEAL_PHOTO", "Failed to open camera", e);
            Toast.makeText(this, "Cannot open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPhotoToStorage(Uri uri) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String path = "meal_photos/" + uid + "/" + System.currentTimeMillis() + ".jpg";

        FirebaseStorage.getInstance()
                .getReference(path)
                .putFile(uri)
                .addOnSuccessListener(task ->
                        task.getStorage().getDownloadUrl()
                                .addOnSuccessListener(download -> {
                                    lastUploadedPhotoUrl = download.toString();
                                    Toast.makeText(this, "Photo uploaded ✔️", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MEAL_PHOTO", "Failed to get download URL", e);
                                    Toast.makeText(this, "Upload ok, but URL failed", Toast.LENGTH_SHORT).show();
                                })
                )
                .addOnFailureListener(e -> {
                    Log.e("MEAL_PHOTO", "Upload failed", e);
                    Toast.makeText(this, "Photo upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveMealToFirestore() {
        String mealName = editMealName.getText().toString().trim();
        String caloriesStr = editMealCalories.getText().toString().trim();

        if (mealName.isEmpty() || caloriesStr.isEmpty()) {
            Toast.makeText(this, "Please enter both name and calories", Toast.LENGTH_SHORT).show();
            return;
        }

        int calories;
        try {
            calories = Integer.parseInt(caloriesStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Calories must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        Meal meal = new Meal();
        meal.setName(mealName);
        meal.setCalories(calories);
        meal.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        meal.setPhotoUrl(lastUploadedPhotoUrl); // may be null (no photo)

        FirebaseFirestore.getInstance()
                .collection("meals")
                .add(meal)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Meal saved", Toast.LENGTH_SHORT).show();
                    editMealName.setText("");
                    editMealCalories.setText("");
                    lastUploadedPhotoUrl = null;
                    fetchMealsFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_MEALS", "Failed to save meal", e);
                    Toast.makeText(this, "Failed to save meal", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchMealsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("meals")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    mealList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Meal meal = doc.toObject(Meal.class);
                        if (meal != null) mealList.add(meal);
                    }
                    adapter = new MealAdapter(this, mealList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_MEALS", "Error loading meals", e);
                    Toast.makeText(this, "Failed to load meals", Toast.LENGTH_SHORT).show();
                });
    }
}

