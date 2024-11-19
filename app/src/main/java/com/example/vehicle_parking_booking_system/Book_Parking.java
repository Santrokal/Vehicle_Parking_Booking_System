package com.example.vehicle_parking_booking_system;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.util.Calendar;

public class Book_Parking extends AppCompatActivity {

    private EditText etPincode;
    private RadioGroup rgVehicleType;
    private Button btnStartTime, btnEndTime, btnBookParking;
    private TextView tvWalletBalance;
    private String startTime, endTime;
    private double walletBalance = 0;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_parking);

        // Initialize UI elements
        etPincode = findViewById(R.id.etPincode);
        rgVehicleType = findViewById(R.id.rgVehicleType);
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndTime = findViewById(R.id.btnEndTime);
        btnBookParking = findViewById(R.id.btnBookParking);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            // Fetch wallet balance
            databaseReference.child("wallet").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        walletBalance = snapshot.getValue(Double.class);
                        tvWalletBalance.setText("Wallet Balance: ₹" + walletBalance);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Book_Parking.this, "Failed to fetch wallet balance.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Time Pickers
        btnStartTime.setOnClickListener(v -> showTimePicker(true));
        btnEndTime.setOnClickListener(v -> showTimePicker(false));

        // Book Parking
        btnBookParking.setOnClickListener(v -> bookParking());
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute1);
            if (isStartTime) {
                startTime = time;
                btnStartTime.setText("Start Time: " + startTime);
            } else {
                endTime = time;
                btnEndTime.setText("End Time: " + endTime);
            }
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void bookParking() {
        String pincode = etPincode.getText().toString().trim();
        int selectedVehicleTypeId = rgVehicleType.getCheckedRadioButtonId();
        RadioButton selectedVehicleType = findViewById(selectedVehicleTypeId);
        String vehicleType = selectedVehicleType != null ? selectedVehicleType.getText().toString() : "";

        if (pincode.isEmpty() || startTime == null || endTime == null || vehicleType.isEmpty()) {
            Toast.makeText(this, "Please fill all the details.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assuming cost is calculated as ₹10/hour for demonstration
        double bookingCost = 10.0 * 2; // Replace with actual calculation

        if (walletBalance >= bookingCost) {
            Toast.makeText(this, "Booking successful!", Toast.LENGTH_SHORT).show();

            // Deduct amount from wallet and update Firebase
            walletBalance -= bookingCost;
            databaseReference.child("wallet").setValue(walletBalance);

            // Add booking to database (Example code)
            DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings");
            String bookingId = bookingsRef.push().getKey();
            Booking booking = new Booking(pincode, vehicleType, startTime, endTime, bookingCost);
            bookingsRef.child(bookingId);
            // Save the booking information in the database
            if (bookingId != null) {
                bookingsRef.child(bookingId).setValue(booking).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Book_Parking.this, "Booking saved successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after successful booking
                    } else {
                        Toast.makeText(Book_Parking.this, "Failed to save booking.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(Book_Parking.this, "Error generating booking ID.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Insufficient balance in wallet. Please add more funds.", Toast.LENGTH_SHORT).show();
        }
    }

    // Booking class for storing booking details
    public static class Booking {
        public String pincode;
        public String vehicleType;
        public String startTime;
        public String endTime;
        public double cost;

        public Booking() {
            // Default constructor required for Firebase
        }

        public Booking(String pincode, String vehicleType, String startTime, String endTime, double cost) {
            this.pincode = pincode;
            this.vehicleType = vehicleType;
            this.startTime = startTime;
            this.endTime = endTime;
            this.cost = cost;
        }
    }
}

