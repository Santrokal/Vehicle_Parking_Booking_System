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

    private TextView tvUserDetails, tvActiveBookings, tvFeedbacks, tvRecharges, tvCanceledBookings;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersReference, bookingsReference, feedbackReference, rechargesReference, canceledBookingsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        tvUserDetails = findViewById(R.id.tvUserDetails);
        tvActiveBookings = findViewById(R.id.tvActiveBookings);
        tvFeedbacks = findViewById(R.id.tvFeedbacks);
        tvRecharges = findViewById(R.id.tvRecharges);
        tvCanceledBookings = findViewById(R.id.tvCanceledBookings);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();

        // Initialize database references
        usersReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        bookingsReference = FirebaseDatabase.getInstance().getReference("Bookings");
        feedbackReference = FirebaseDatabase.getInstance().getReference("Feedback");
        rechargesReference = FirebaseDatabase.getInstance().getReference("Recharges").child(userId);
        canceledBookingsReference = FirebaseDatabase.getInstance().getReference("CanceledBookings").child(userId);

        // Fetch data
        fetchUserDetails();
        fetchBookings();
        fetchFeedbacks();
        fetchRecharges();
        fetchCanceledBookings();
    }

    private void fetchUserDetails() {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    tvUserDetails.setText("Name: " + name + "\nEmail: " + email);
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

    private void fetchBookings() {
        bookingsReference.orderByChild("userId").equalTo(firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StringBuilder bookingsData = new StringBuilder();
                        for (DataSnapshot booking : snapshot.getChildren()) {
                            String location = booking.child("location").getValue(String.class);
                            String startTime = booking.child("startTime").getValue(String.class);

                            bookingsData.append("Location: ").append(location).append("\n")
                                    .append("Start Time: ").append(startTime).append("\n\n");
                        }
                        tvActiveBookings.setText(bookingsData.length() > 0 ? bookingsData.toString() : "No Active Bookings");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Profile.this, "Failed to fetch bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchFeedbacks() {
        feedbackReference.orderByChild("userName").equalTo(firebaseAuth.getCurrentUser().getDisplayName())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StringBuilder feedbackData = new StringBuilder();
                        for (DataSnapshot feedback : snapshot.getChildren()) {
                            String rating = feedback.child("rating").getValue(String.class);
                            String comments = feedback.child("comments").getValue(String.class);

                            feedbackData.append("Rating: ").append(rating).append("\n")
                                    .append("Comments: ").append(comments).append("\n\n");
                        }
                        tvFeedbacks.setText(feedbackData.length() > 0 ? feedbackData.toString() : "No Feedback Provided");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Profile.this, "Failed to fetch feedback: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchRecharges() {
        rechargesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder rechargeData = new StringBuilder();
                for (DataSnapshot recharge : snapshot.getChildren()) {
                    String amount = recharge.child("amount").getValue(String.class);
                    String date = recharge.child("date").getValue(String.class);

                    rechargeData.append("Amount: ₹").append(amount).append("\n")
                            .append("Date: ").append(date).append("\n\n");
                }
                tvRecharges.setText(rechargeData.length() > 0 ? rechargeData.toString() : "No Recharges Found");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to fetch recharges: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

                    canceledData.append("Location: ").append(location).append("\n")
                            .append("Cancelled On: ").append(cancelDate).append("\n\n");
                }
                tvCanceledBookings.setText(canceledData.length() > 0 ? canceledData.toString() : "No Cancelled Bookings");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to fetch canceled bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
