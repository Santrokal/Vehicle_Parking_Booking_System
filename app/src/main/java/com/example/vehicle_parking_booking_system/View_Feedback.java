package com.example.vehicle_parking_booking_system;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class View_Feedback extends AppCompatActivity {

    private LinearLayout feedbackContainer;
    private DatabaseReference feedbackDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feedback);

        feedbackContainer = findViewById(R.id.feedbackContainer);

        // Initialize Firebase Database Reference
        feedbackDatabaseRef = FirebaseDatabase.getInstance().getReference("Feedback");

        // Fetch feedback from Firebase
        fetchFeedbackFromDatabase();
    }

    private void fetchFeedbackFromDatabase() {
        feedbackDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                feedbackContainer.removeAllViews(); // Clear previous feedback
                for (DataSnapshot feedbackSnapshot : snapshot.getChildren()) {
                    // Extract feedback details
                    String name = feedbackSnapshot.child("name").getValue(String.class);
                    String comment = feedbackSnapshot.child("comment").getValue(String.class);
                    Float rating = feedbackSnapshot.child("rating").getValue(Float.class);

                    if (name == null || comment == null || rating == null) continue; // Skip incomplete data

                    // Create a TextView for each feedback entry
                    TextView feedbackView = new TextView(View_Feedback.this);
                    feedbackView.setText("Name: " + name + "\n"
                            + "Comment: " + comment + "\n"
                            + "Rating: " + rating + " ★");
                    feedbackView.setPadding(16, 16, 16, 16);
                    feedbackView.setTextSize(16f);
                    feedbackView.setTextColor(getResources().getColor(android.R.color.black));
                    feedbackView.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));
                    feedbackView.setGravity(Gravity.START);
                    feedbackView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));

                    // Add each feedback to the container
                    feedbackContainer.addView(feedbackView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_Feedback.this, "Failed to load feedback: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
