package com.example.vehicle_parking_booking_system;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Cancel_Parking extends AppCompatActivity {

    private RecyclerView recyclerViewBookings;
    private TextView textViewNoBookings;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList = new ArrayList<>();
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_parking);

        // Initialize views
        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        textViewNoBookings = findViewById(R.id.textViewNoBookings);

        // Set up RecyclerView
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Bookings");

        // Fetch bookings from Firebase
        fetchBookings();
    }

    private void fetchBookings() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear(); // Clear any previous data

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

                    // Initialize the adapter and set it to the RecyclerView
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
        // Remove booking from Firebase
        databaseReference.child(booking.getBookingId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Cancel_Parking.this, "Booking cancelled!", Toast.LENGTH_SHORT).show();

                    // Update UI after removal
                    bookingList.remove(booking);
                    bookingAdapter.notifyDataSetChanged();

                    if (bookingList.isEmpty()) {
                        textViewNoBookings.setVisibility(View.VISIBLE);
                        recyclerViewBookings.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Cancel_Parking.this, "Failed to cancel booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
