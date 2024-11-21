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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
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

        // Initialize Firebase references
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        bookingsReference = FirebaseDatabase.getInstance().getReference("Bookings");
        canceledBookingsReference = FirebaseDatabase.getInstance().getReference("CanceledBookings");
        usersReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Fetch bookings
        fetchBookings();
    }

    private void fetchBookings() {
        bookingsReference.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookingList.clear();

                        for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                            // Extract booking details
                            String bookingId = bookingSnapshot.getKey();
                            String location = bookingSnapshot.child("location").getValue(String.class);
                            String startTime = bookingSnapshot.child("startTime").getValue(String.class);
                            String endTime = bookingSnapshot.child("endTime").getValue(String.class);
                            Double parkingFee = bookingSnapshot.child("parkingFee").getValue(Double.class);
                            String vehicleType = bookingSnapshot.child("vehicleType").getValue(String.class);

                            // Validate and add booking to the list
                            if (bookingId != null && location != null && startTime != null && endTime != null && parkingFee != null && vehicleType != null) {
                                bookingList.add(new Booking(bookingId, location, startTime, endTime, parkingFee, userId, vehicleType));
                            }
                        }

                        // Update RecyclerView or show "No Bookings" message
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
        usersReference.child("walletBalance").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Double currentBalance = mutableData.getValue(Double.class);
                if (currentBalance == null) {
                    currentBalance = 0.0;
                }
                mutableData.setValue(currentBalance + booking.getParkingFee());
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (committed) {
                    // Record canceled booking
                    String canceledDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    CanceledBooking canceledBooking = new CanceledBooking(
                            booking.getBookingId(),
                            booking.getLocation(),
                            canceledDateTime,
                            booking.getParkingFee(),
                            booking.getUserName(),
                            null, // Email (not provided in structure)
                            null  // Phone (not provided in structure)
                    );

                    canceledBookingsReference.child(userId).child(booking.getBookingId())
                            .setValue(canceledBooking)
                            .addOnSuccessListener(aVoid -> {
                                // Remove booking from active bookings
                                bookingsReference.child(booking.getBookingId()).removeValue()
                                        .addOnSuccessListener(aVoid1 -> {
                                            Toast.makeText(Cancel_Parking.this, "Booking canceled and amount refunded to wallet!", Toast.LENGTH_SHORT).show();

                                            // Update UI
                                            bookingList.remove(booking);
                                            bookingAdapter.notifyDataSetChanged();

                                            if (bookingList.isEmpty()) {
                                                textViewNoBookings.setVisibility(View.VISIBLE);
                                                recyclerViewBookings.setVisibility(View.GONE);
                                            }
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(Cancel_Parking.this, "Failed to remove booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(Cancel_Parking.this, "Failed to record canceled booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(Cancel_Parking.this, "Failed to update wallet balance", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
