package com.example.vehicle_parking_booking_system;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class View_Booking extends AppCompatActivity {

    private Button btnSelectDate;
    private TextView tvSelectedDate, tvBookingCount;
    private LinearLayout bookingContainer;

    private DatabaseReference bookingsDatabaseRef;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_booking);

        // Initialize UI components
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvBookingCount = findViewById(R.id.tvBookingCount);
        bookingContainer = findViewById(R.id.bookingContainer);

        // Firebase Database Reference
        bookingsDatabaseRef = FirebaseDatabase.getInstance().getReference("Bookings");

        // Set up date picker dialog
        btnSelectDate.setOnClickListener(v -> showDatePicker());

        // Default selected date is today
        Calendar calendar = Calendar.getInstance();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        tvSelectedDate.setText("Selected Date: " + selectedDate);
        fetchBookingsForDate(selectedDate);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Update selected date
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.getTime());

            // Update UI and fetch bookings
            tvSelectedDate.setText("Selected Date: " + selectedDate);
            fetchBookingsForDate(selectedDate);

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void fetchBookingsForDate(String date) {
        bookingsDatabaseRef.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingContainer.removeAllViews(); // Clear previous data
                int bookingCount = 0;

                for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                    // Extract booking details
                    String name = bookingSnapshot.child("name").getValue(String.class);
                    String slot = bookingSnapshot.child("slot").getValue(String.class);
                    String vehicleNumber = bookingSnapshot.child("vehicleNumber").getValue(String.class);

                    // Create a TextView for each booking
                    TextView bookingView = new TextView(View_Booking.this);
                    bookingView.setText("Name: " + name + "\n"
                            + "Slot: " + slot + "\n"
                            + "Vehicle: " + vehicleNumber);
                    bookingView.setPadding(16, 16, 16, 16);
                    bookingView.setTextSize(16f);
                    bookingView.setTextColor(getResources().getColor(android.R.color.black));
                    bookingView.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));

                    // Add booking details to the container
                    bookingContainer.addView(bookingView);

                    bookingCount++;
                }

                // Update booking count
                tvBookingCount.setText("Total Bookings: " + bookingCount);

                if (bookingCount == 0) {
                    Toast.makeText(View_Booking.this, "No bookings found for the selected date.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_Booking.this, "Failed to fetch bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
