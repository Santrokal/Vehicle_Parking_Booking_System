package com.example.vehicle_parking_booking_system;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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

    private Spinner spinnerLocations;
    private RadioGroup rgVehicleType;
    private Button btnStartTime, btnEndTime, btnBookParking;
    private TextView tvWalletBalance;

    private String startTime, endTime;
    private double walletBalance = 0;
    private boolean isReturningUser = false; // Loyalty check

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userReference, bookingReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_parking);

        // Initialize UI components
        spinnerLocations = findViewById(R.id.spinnerLocations);
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
            userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            bookingReference = FirebaseDatabase.getInstance().getReference("Bookings");

            // Fetch wallet balance and display
            fetchWalletBalance();

            // Check if the user has previous bookings for the loyalty discount
            checkReturningUser(userId);
        }

        // Load locations into the Spinner
        loadLocations();

        // Time Pickers for Start and End Time
        btnStartTime.setOnClickListener(v -> showTimePicker(true));
        btnEndTime.setOnClickListener(v -> showTimePicker(false));

        // Book parking button click listener
        btnBookParking.setOnClickListener(v -> {
            String selectedLocation = (String) spinnerLocations.getSelectedItem();
            if (selectedLocation == null || selectedLocation.isEmpty()) {
                Toast.makeText(this, "Please select a location.", Toast.LENGTH_SHORT).show();
                return;
            }
            bookParking(selectedLocation);
        });
    }

    private void fetchWalletBalance() {
        // Use ValueEventListener to listen for real-time changes
        userReference.child("walletBalance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    walletBalance = snapshot.getValue(Double.class);
                    tvWalletBalance.setText("Wallet Balance: ₹" + walletBalance);
                } else {
                    tvWalletBalance.setText("Wallet Balance: ₹0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Book_Parking.this, "Failed to fetch wallet balance: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkReturningUser(String userId) {
        bookingReference.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isReturningUser = snapshot.exists(); // True if user has previous bookings
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Book_Parking.this, "Failed to check returning user: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLocations() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.pincode_locations,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocations.setAdapter(adapter);
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

    private void bookParking(String location) {
        int selectedVehicleTypeId = rgVehicleType.getCheckedRadioButtonId();
        RadioButton selectedVehicleType = findViewById(selectedVehicleTypeId);
        String vehicleType = selectedVehicleType != null ? selectedVehicleType.getText().toString() : "";

        if (startTime == null || endTime == null || vehicleType.isEmpty()) {
            Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        long duration = calculateTimeDuration(startTime, endTime);
        if (duration <= 0) {
            Toast.makeText(this, "Invalid time duration. Please check your times.", Toast.LENGTH_SHORT).show();
            return;
        }

        double parkingFee = calculateParkingAmount(duration, vehicleType);

        if (walletBalance >= parkingFee) {
            walletBalance -= parkingFee;
            userReference.child("walletBalance").setValue(walletBalance);

            // Retrieve the user's name
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            String userName = firebaseUser != null ? firebaseUser.getDisplayName() : "Unknown";

            String bookingId = bookingReference.push().getKey();
            Booking booking = new Booking(
                    bookingId,
                    firebaseAuth.getCurrentUser().getUid(),
                    location,
                    vehicleType,
                    startTime,
                    endTime,
                    parkingFee,
                    userName // Store the user's name
            );

            bookingReference.child(bookingId).setValue(booking).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(Book_Parking.this, Success_Booking.class);
                    intent.putExtra("bookingId", bookingId);
                    intent.putExtra("location", location);
                    intent.putExtra("startTime", startTime);
                    intent.putExtra("endTime", endTime);
                    intent.putExtra("parkingFee", parkingFee);
                    startActivity(intent);
                    Toast.makeText(Book_Parking.this, "Parking booked successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Book_Parking.this, "Failed to book parking.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Insufficient wallet balance.", Toast.LENGTH_SHORT).show();
        }
    }

    private long calculateTimeDuration(String startTime, String endTime) {
        try {
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);

            long startInMinutes = startHour * 60 + startMinute;
            long endInMinutes = endHour * 60 + endMinute;
            return endInMinutes - startInMinutes;
        } catch (Exception e) {
            Toast.makeText(this, "Failed to calculate time duration.", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    private double calculateParkingAmount(long duration, String vehicleType) {
        double baseRate, additionalRate;

        if (vehicleType.equalsIgnoreCase("Car")) {
            baseRate = 200;
            additionalRate = 80;
        } else {
            baseRate = 100;
            additionalRate = 40;
        }

        double parkingFee = baseRate + Math.max(0, (duration - 60) / 60) * additionalRate;

        // Apply 20% discount if user is returning
        if (isReturningUser) {
            parkingFee *= 0.8;
        }

        return parkingFee;
    }

    public static class Booking {
        private String bookingId;
        private String userId;
        private String location;
        private String vehicleType;
        private String startTime;
        private String endTime;
        private double parkingFee;
        private String userName; // Store the user's name

        public Booking() {
        }

        public Booking(String bookingId, String userId, String location, String vehicleType, String startTime, String endTime, double parkingFee, String userName) {
            this.bookingId = bookingId;
            this.userId = userId;
            this.location = location;
            this.vehicleType = vehicleType;
            this.startTime = startTime;
            this.endTime = endTime;
            this.parkingFee = parkingFee;
            this.userName = userName; // Initialize user name
        }

        public String getBookingId() {
            return bookingId;
        }

        public String getUserId() {
            return userId;
        }

        public String getLocation() {
            return location;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public double getParkingFee() {
            return parkingFee;
        }

        public String getUserName() {
            return userName; // Getter for user name
        }
    }
}
