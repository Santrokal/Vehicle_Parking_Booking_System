package com.example.vehicle_parking_booking_system;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class View_Booking extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookingAdapter1 bookingAdapter;
    private List<Booking1> bookingList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference bookingReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_booking);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        bookingReference = FirebaseDatabase.getInstance().getReference("Bookings");

        // Initialize booking list and adapter
        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter1(this, bookingList);
        recyclerView.setAdapter(bookingAdapter);

        // Fetch and display bookings
        fetchBookings(userId);
    }

    private void fetchBookings(String userId) {
        bookingReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear(); // Clear the list before adding new data
                if (snapshot.exists()) {
                    for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                        Booking1 booking = bookingSnapshot.getValue(Booking1.class);
                        bookingList.add(booking);
                    }
                    bookingAdapter.notifyDataSetChanged(); // Notify adapter of data change
                } else {
                    Toast.makeText(View_Booking.this, "No bookings found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_Booking.this, "Failed to fetch bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
