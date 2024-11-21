package com.example.vehicle_parking_booking_system;

import android.os.Bundle;
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

public class Profile extends AppCompatActivity {

    private TextView tvUserDetails, tvWalletBalance, tvActiveBookings, tvCanceledBookings;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersReference, bookingsReference, canceledBookingsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        tvUserDetails = findViewById(R.id.tvUserDetails);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvActiveBookings = findViewById(R.id.tvActiveBookings);
        tvCanceledBookings = findViewById(R.id.tvCanceledBookings);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();

        // Initialize database references
        usersReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        bookingsReference = FirebaseDatabase.getInstance().getReference("Bookings");
        canceledBookingsReference = FirebaseDatabase.getInstance().getReference("CanceledBookings").child(userId);

        // Fetch and display user data
        fetchUserDetails();
        fetchWalletBalance();
        fetchActiveBookings();
        fetchCanceledBookings();
    }

    private void fetchUserDetails() {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);

                    // Display user details
                    tvUserDetails.setText("Name: " + (name != null ? name : "N/A") + "\n" +
                            "Email: " + (email != null ? email : "N/A") + "\n" +
                            "Phone: " + (phone != null ? phone : "N/A"));
                } else {
                    tvUserDetails.setText("User details not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to fetch user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWalletBalance() {
        usersReference.child("walletBalance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double walletBalance = snapshot.getValue(Double.class);
                    tvWalletBalance.setText("Wallet Balance: ₹" + (walletBalance != null ? walletBalance : 0.0));
                } else {
                    tvWalletBalance.setText("Wallet Balance: ₹0.0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to fetch wallet balance: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchActiveBookings() {
        bookingsReference.orderByChild("userId").equalTo(firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StringBuilder bookingsData = new StringBuilder();
                        for (DataSnapshot booking : snapshot.getChildren()) {
                            String location = booking.child("location").getValue(String.class);
                            String startTime = booking.child("startTime").getValue(String.class);

                            bookingsData.append("Location: ").append(location != null ? location : "N/A").append("\n")
                                    .append("Start Time: ").append(startTime != null ? startTime : "N/A").append("\n\n");
                        }
                        tvActiveBookings.setText(bookingsData.length() > 0 ? bookingsData.toString() : "No Active Bookings");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Profile.this, "Failed to fetch bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchCanceledBookings() {
        canceledBookingsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder canceledData = new StringBuilder();
                for (DataSnapshot booking : snapshot.getChildren()) {
                    String location = booking.child("location").getValue(String.class);
                    String cancelDate = booking.child("cancelDate").getValue(String.class);

                    canceledData.append("Location: ").append(location != null ? location : "N/A").append("\n")
                            .append("Cancelled On: ").append(cancelDate != null ? cancelDate : "N/A").append("\n\n");
                }
                tvCanceledBookings.setText(canceledData.length() > 0 ? canceledData.toString() : "No Canceled Bookings");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to fetch canceled bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
