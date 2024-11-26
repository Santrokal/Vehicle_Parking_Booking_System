package com.example.vehicle_parking_booking_system;

import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class View_Booking extends AppCompatActivity {

    private ListView listViewBookings;
    private List<Booking1> bookingList;

    private DatabaseReference bookingReference;
    private Button btnSelectDate;
    private TextView tvSelectedDate, tvBookingCountToday;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_booking);

        // Initialize UI components
        listViewBookings = findViewById(R.id.lvBookings);
        bookingList = new ArrayList<>();
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvBookingCountToday = findViewById(R.id.tvBookingCountToday);

        // Initialize Firebase
        bookingReference = FirebaseDatabase.getInstance().getReference("Bookings");

        // Set today's date as default filter
        selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        tvSelectedDate.setText("Selected Date: " + selectedDate);

        // Fetch bookings and count for today
        fetchBookings(selectedDate);
        fetchBookingCountToday();

        // DatePicker button listener
        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            // Update the selected date
            selectedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear);
            tvSelectedDate.setText("Selected Date: " + selectedDate);

            // Fetch bookings for the selected date
            fetchBookings(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void fetchBookings(String dateFilter) {
        bookingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                        Booking1 booking = bookingSnapshot.getValue(Booking1.class);
                        if (booking != null && booking.getDate().equals(dateFilter)) {
                            bookingList.add(booking);
                        }
                    }
                    BookingAdapter1 adapter = new BookingAdapter1(View_Booking.this, bookingList);
                    listViewBookings.setAdapter(adapter);
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

    private void fetchBookingCountToday() {
        String todayDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        bookingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                        Booking1 booking = bookingSnapshot.getValue(Booking1.class);
                        if (booking != null && booking.getDate().equals(todayDate)) {
                            count++;
                        }
                    }
                }
                tvBookingCountToday.setText("Bookings Today: " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_Booking.this, "Failed to fetch today's bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
