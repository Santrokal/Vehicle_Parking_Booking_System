package com.example.vehicle_parking_booking_system;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting, tvUserName, tvDateTime;
    private Button btnBookParking, btnlogout, btnCostCalculation, btnCancelParking, btnProfile, btnFeedback, btnAddBalance;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);
        tvDateTime = findViewById(R.id.tvDateTime);

        btnBookParking = findViewById(R.id.btnBookParking);
        btnCostCalculation = findViewById(R.id.btnCostCalculation);
        btnCancelParking = findViewById(R.id.btnCancelParking);
        btnProfile = findViewById(R.id.btnProfile);
        btnFeedback = findViewById(R.id.btnFeedback);
        btnAddBalance = findViewById(R.id.btnAddBalance);
        btnlogout = findViewById(R.id.btnlogout);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            // Fetch user's name
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        tvUserName.setText(name != null ? name : "User");
                    } else {
                        tvUserName.setText("User");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Display current date and time
        updateDateTime();

        // Button click listeners with navigation
        btnBookParking.setOnClickListener(v -> {
            Toast.makeText(this, "Navigating to Book Parking", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Book_Parking.class));
        });


        btnCostCalculation.setOnClickListener(v -> {
            Toast.makeText(this, "Navigating to Cost Calculation", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Cost_Calculation.class));
        });

        btnCancelParking.setOnClickListener(v -> {
            Toast.makeText(this, "Navigating to Parking Cancellation", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Cancel_Parking.class));
        });

        btnProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Navigating to Profile", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Profile.class));
        });

        btnFeedback.setOnClickListener(v -> {
            Toast.makeText(this, "Navigating to Feedback", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Feedback.class));
        });

        btnAddBalance.setOnClickListener(v -> {
            Toast.makeText(this, "Navigating to Add Balance", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Add_Balance.class));
        });
        btnlogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logout Success", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, user_login.class));
        });
    }

    private void updateDateTime() {
        // Format and display the current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy hh:mm a", Locale.getDefault());
        String currentDateTime = dateFormat.format(new Date());
        tvDateTime.setText(currentDateTime);
    }
}
