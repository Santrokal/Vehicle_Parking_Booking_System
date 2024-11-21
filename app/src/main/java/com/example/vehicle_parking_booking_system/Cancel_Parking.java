package com.example.vehicle_parking_booking_system;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Cancel_Parking extends AppCompatActivity {

    private RecyclerView recyclerViewBookings;
    private TextView textViewNoBookings;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList = new ArrayList<>();
    private DatabaseReference bookingsReference, canceledBookingsReference, usersReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_parking);

        // Initialize views
        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        textViewNoBookings = findViewById(R.id.textViewNoBookings);

        // Set up RecyclerView
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase Database references
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        bookingsReference = FirebaseDatabase.getInstance().getReference("Bookings");
        canceledBookingsReference = FirebaseDatabase.getInstance().getReference("CanceledBookings");
        usersReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Fetch bookings from Firebase
        fetchBookings();
    }

    private void fetchBookings() {
        bookingsReference.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookingList.clear();

                        for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                            String bookingId = bookingSnapshot.getKey();
                            String location = bookingSnapshot.child("location").getValue(String.class);
                            String startTime = bookingSnapshot.child("startTime").getValue(String.class);
                            String endTime = bookingSnapshot.child("endTime").getValue(String.class);
                            double parkingFee = bookingSnapshot.child("parkingFee").getValue(Double.class);
                            String userName = bookingSnapshot.child("userName").getValue(String.class);
                            String vehicleType = bookingSnapshot.child("vehicleType").getValue(String.class);

                            if (bookingId != null && location != null && startTime != null && endTime != null && userName != null && vehicleType != null) {
                                bookingList.add(new Booking(bookingId, location, startTime, endTime, parkingFee, userName, vehicleType));
                            }
                        }

                        // Update the RecyclerView and display No Bookings message if needed
                        if (bookingList.isEmpty()) {
                            textViewNoBookings.setVisibility(View.VISIBLE);
                            recyclerViewBookings.setVisibility(View.GONE);
                        } else {
                            textViewNoBookings.setVisibility(View.GONE);
                            recyclerViewBookings.setVisibility(View.VISIBLE);

                            bookingAdapter = new BookingAdapter(bookingList, Cancel_Parking.this::cancelBooking);
                            recyclerViewBookings.setAdapter(bookingAdapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Cancel_Parking.this, "Failed to fetch bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancelBooking(Booking booking) {
        // Fetch user details
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    Double walletBalance = snapshot.child("walletBalance").getValue(Double.class);

                    // Set default values if any field is null
                    name = name != null ? name : "Unknown User";
                    email = email != null ? email : "Unknown Email";
                    phone = phone != null ? phone : "Unknown Phone";
                    walletBalance = walletBalance != null ? walletBalance : 0.0;

                    // Log the data for debugging
                    System.out.println("User Details: " + name + ", " + email + ", " + phone + ", Wallet: " + walletBalance);

                    // Store canceled booking details
                    String canceledDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    CanceledBooking canceledBooking = new CanceledBooking(
                            booking.getBookingId(),
                            booking.getLocation(),
                            canceledDateTime,
                            booking.getParkingFee(),
                            name,
                            email,
                            phone
                    );

                    Double finalWalletBalance = walletBalance;
                    canceledBookingsReference.child(userId).child(booking.getBookingId())
                            .setValue(canceledBooking)
                            .addOnSuccessListener(aVoid -> {
                                // Update wallet balance
                                double updatedBalance = finalWalletBalance + booking.getParkingFee();
                                usersReference.child("walletBalance").setValue(updatedBalance)
                                        .addOnSuccessListener(aVoid1 -> {
                                            // Remove booking from "Bookings"
                                            bookingsReference.child(booking.getBookingId()).removeValue()
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        Toast.makeText(Cancel_Parking.this, "Booking canceled and amount refunded to wallet!", Toast.LENGTH_SHORT).show();

                                                        // Update UI after removal
                                                        bookingList.remove(booking);
                                                        bookingAdapter.notifyDataSetChanged();

                                                        if (bookingList.isEmpty()) {
                                                            textViewNoBookings.setVisibility(View.VISIBLE);
                                                            recyclerViewBookings.setVisibility(View.GONE);
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        System.err.println("Error removing booking: " + e.getMessage());
                                                        Toast.makeText(Cancel_Parking.this, "Failed to cancel booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            System.err.println("Error updating wallet balance: " + e.getMessage());
                                            Toast.makeText(Cancel_Parking.this, "Failed to update wallet balance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                System.err.println("Error storing canceled booking: " + e.getMessage());
                                Toast.makeText(Cancel_Parking.this, "Failed to store canceled booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(Cancel_Parking.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Error fetching user details: " + error.getMessage());
                Toast.makeText(Cancel_Parking.this, "Failed to fetch user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
