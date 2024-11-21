package com.example.vehicle_parking_booking_system;

import android.app.TimePickerDialog;
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
import java.util.concurrent.TimeUnit;

public class Book_Parking extends AppCompatActivity {

    private Spinner spinnerLocations;
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

        // Load locations from arrays.xml into the Spinner
        loadLocations();

        // Time Pickers
        btnStartTime.setOnClickListener(v -> showTimePicker(true));
        btnEndTime.setOnClickListener(v -> showTimePicker(false));

        // Book Parking
        btnBookParking.setOnClickListener(v -> {
            String selectedLocation = (String) spinnerLocations.getSelectedItem();
            if (selectedLocation == null || selectedLocation.isEmpty()) {
                Toast.makeText(this, "Please select a location.", Toast.LENGTH_SHORT).show();
                return;
            }
            bookParking(selectedLocation);
        });
    }

    private void loadLocations() {
        // Fetch locations from arrays.xml
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

        // Calculate parking fee based on time and vehicle type
        long duration = calculateTimeDuration(startTime, endTime);
        double parkingFee = calculateParkingAmount(duration, vehicleType);

        // Apply discount for bike users
        if (vehicleType.equalsIgnoreCase("Bike")) {
            parkingFee *= 0.8; // 20% discount for bikes
        }

        if (walletBalance >= parkingFee) {
            // Deduct amount from wallet balance
            walletBalance -= parkingFee;
            databaseReference.child("wallet").setValue(walletBalance);

            // Fetch user's name
            double finalParkingFee = parkingFee;
            databaseReference.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userName = snapshot.getValue(String.class);

                        // Create a booking object to store
                        DatabaseReference bookingReference = FirebaseDatabase.getInstance().getReference("Bookings").push();
                        String bookingId = bookingReference.getKey();

                        Booking booking = new Booking(
                                bookingId,
                                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                userName, // Pass the user's name
                                vehicleType,
                                startTime,
                                endTime,
                                location,
                                finalParkingFee
                        );

                        // Save booking to Firebase, including parking cost
                        bookingReference.setValue(booking).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(Book_Parking.this, "Parking booked successfully at " + location, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Book_Parking.this, "Failed to book parking. Try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(Book_Parking.this, "User name not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Book_Parking.this, "Failed to fetch user name.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(Book_Parking.this, "Insufficient balance for booking.", Toast.LENGTH_SHORT).show();
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
            return 0;
        }
    }

    private double calculateParkingAmount(long duration, String vehicleType) {
        double rate = 0;

        if (vehicleType.equalsIgnoreCase("Car")) {
            if (duration <= 60) {
                rate = 250;
            } else {
                rate = 80 * (duration / 60);
            }
        } else if (vehicleType.equalsIgnoreCase("Bike")) {
            if (duration <= 60) {
                rate = 150;
            } else {
                rate = 60 * (duration / 60);
            }
        }

        return rate;
    }

    public class Booking {
        private String bookingId;
        private String userId;
        private String userName; // New field for user's name
        private String vehicleType;
        private String startTime;
        private String endTime;
        private String location;
        private double parkingFee; // New field for parking fee

        // Default constructor (required for Firebase)
        public Booking() {}

        public Booking(String bookingId, String userId, String userName, String vehicleType, String startTime, String endTime, String location, double parkingFee) {
            this.bookingId = bookingId;
            this.userId = userId;
            this.userName = userName;
            this.vehicleType = vehicleType;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
            this.parkingFee = parkingFee;
        }

        // Getters and setters
        public String getBookingId() {
            return bookingId;
        }

        public void setBookingId(String bookingId) {
            this.bookingId = bookingId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public void setVehicleType(String vehicleType) {
            this.vehicleType = vehicleType;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public double getParkingFee() {
            return parkingFee;
        }

        public void setParkingFee(double parkingFee) {
            this.parkingFee = parkingFee;
        }
    }
}
