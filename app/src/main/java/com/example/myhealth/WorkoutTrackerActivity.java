package com.example.myhealth;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class WorkoutTrackerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WorkoutAdapter adapter;
    private List<Workout> workoutList = new ArrayList<>();

    private EditText editWorkoutName, editWorkoutSets, editWorkoutReps;
    private Button btnAddWorkout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_tracker);

        recyclerView = findViewById(R.id.workoutRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        editWorkoutName = findViewById(R.id.editWorkoutName);
        editWorkoutSets = findViewById(R.id.editWorkoutSets);
        editWorkoutReps = findViewById(R.id.editWorkoutReps);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);

        btnAddWorkout.setOnClickListener(v -> saveWorkoutToFirestore());

        fetchWorkoutsFromFirestore();
    }

    private void saveWorkoutToFirestore() {
        String workoutName = editWorkoutName.getText().toString().trim();
        String setsStr = editWorkoutSets.getText().toString().trim();
        String repsStr = editWorkoutReps.getText().toString().trim();

        if (workoutName.isEmpty() || setsStr.isEmpty() || repsStr.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int sets, reps;
        try {
            sets = Integer.parseInt(setsStr);
            reps = Integer.parseInt(repsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Sets and Reps must be numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        Workout workout = new Workout();
        workout.setName(workoutName);
        workout.setSets(sets);
        workout.setReps(reps);
        workout.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseFirestore.getInstance()
                .collection("workouts")
                .add(workout)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Workout added", Toast.LENGTH_SHORT).show();
                    editWorkoutName.setText("");
                    editWorkoutSets.setText("");
                    editWorkoutReps.setText("");
                    fetchWorkoutsFromFirestore(); // Refresh list
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding workout", e);
                    Toast.makeText(this, "Failed to add workout", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchWorkoutsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("workouts")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    workoutList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Workout workout = doc.toObject(Workout.class);
                        if (workout != null && workout.getName() != null) {
                            workoutList.add(workout);
                        }
                    }

                    adapter = new WorkoutAdapter(workoutList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading workouts", e);
                    Toast.makeText(this, "Failed to load workouts", Toast.LENGTH_SHORT).show();
                });
    }
}


