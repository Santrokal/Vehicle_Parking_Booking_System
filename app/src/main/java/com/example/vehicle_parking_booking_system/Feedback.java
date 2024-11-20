package com.example.vehicle_parking_booking_system;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Feedback extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etFeedback;
    private Button btnSubmitFeedback;
    private TextView tvThankYouMessage;
    private DatabaseReference feedbackReference, usersReference;
    private FirebaseAuth firebaseAuth;
    private String userName = "User"; // Default to 'User' if no name is found

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Initialize views
        ratingBar = findViewById(R.id.ratingBar);
        etFeedback = findViewById(R.id.etFeedback);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);
        tvThankYouMessage = findViewById(R.id.tvThankYouMessage);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        feedbackReference = FirebaseDatabase.getInstance().getReference("Feedback");
        usersReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Fetch user name from Users database
        fetchUserName();

        // Submit feedback button click
        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void fetchUserName() {
        // Fetch user's name from the database
        usersReference.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userName = snapshot.getValue(String.class);
                } else {
                    Toast.makeText(Feedback.this, "User name not found, defaulting to 'User'.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Feedback.this, "Failed to fetch user name: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitFeedback() {
        float rating = ratingBar.getRating();
        String comments = etFeedback.getText().toString().trim();

        // Validate input
        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comments.isEmpty()) {
            Toast.makeText(this, "Please provide your feedback.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate unique feedback ID
        String feedbackId = feedbackReference.push().getKey();

        // Prepare feedback data
        HashMap<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("userName", userName);
        feedbackData.put("rating", rating);
        feedbackData.put("comments", comments);

        // Save feedback to Firebase
        feedbackReference.child(feedbackId).setValue(feedbackData)
                .addOnSuccessListener(aVoid -> {
                    tvThankYouMessage.setText("Thank you for your feedback, " + userName + "!");
                    tvThankYouMessage.setVisibility(TextView.VISIBLE);

                    // Reset fields
                    ratingBar.setRating(0);
                    etFeedback.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(Feedback.this, "Failed to submit feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
