package com.example.vehicle_parking_booking_system;

import android.app.TimePickerDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Book_Parking extends AppCompatActivity {

    private EditText etPincode;
    private RadioGroup rgVehicleType;
    private Button btnStartTime, btnEndTime, btnBookParking;
    private TextView tvWalletBalance;
    private String startTime, endTime;
    private double walletBalance = 0;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private GoogleMap googleMap;

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

        // Initialize Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(map -> {
                googleMap = map;
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            });
        }

        // Time Pickers
        btnStartTime.setOnClickListener(v -> showTimePicker(true));
        btnEndTime.setOnClickListener(v -> showTimePicker(false));

        // Book Parking
        btnBookParking.setOnClickListener(v -> {
            String pincode = etPincode.getText().toString().trim();
            if (pincode.isEmpty()) {
                Toast.makeText(this, "Please enter a valid pincode.", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchParkingSlots(pincode);
            bookParking();
        });
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

    private void fetchParkingSlots(String pincode) {
        if (googleMap == null) {
            Toast.makeText(this, "Map is not ready.", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this);
        try {
            // Get latitude and longitude for the pincode
            List<Address> addresses = geocoder.getFromLocationName(pincode, 1);
            if (addresses.isEmpty()) {
                Toast.makeText(this, "Invalid pincode or location not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = addresses.get(0);
            LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

            // Center the map on the pincode location
            googleMap.clear();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14));

            // Simulate available parking slots (you should replace this with real data from your database)
            List<LatLng> parkingSlots = Arrays.asList(
                    new LatLng(location.latitude + 0.001, location.longitude),
                    new LatLng(location.latitude - 0.001, location.longitude + 0.001),
                    new LatLng(location.latitude, location.longitude - 0.001)
            );

            for (LatLng slot : parkingSlots) {
                googleMap.addMarker(new MarkerOptions()
                        .position(slot)
                        .title("Parking Slot")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }

            // Add a marker at the entered pincode
            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Entered Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        } catch (Exception e) {
            Toast.makeText(this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

        // Calculate parking duration
        String[] startSplit = startTime.split(":");
        String[] endSplit = endTime.split(":");
        int startHour = Integer.parseInt(startSplit[0]);
        int startMinute = Integer.parseInt(startSplit[1]);
        int endHour = Integer.parseInt(endSplit[0]);
        int endMinute = Integer.parseInt(endSplit[1]);

        int totalMinutes = (endHour * 60 + endMinute) - (startHour * 60 + startMinute);
        if (totalMinutes <= 0) {
            Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show();
            return;
        }

        double hours = Math.ceil(totalMinutes / 60.0);

        double baseRate = vehicleType.equalsIgnoreCase("Car") ? 150.0 : 100.0;
        double additionalRate = vehicleType.equalsIgnoreCase("Car") ? 80.0 : 50.0;

        final double[] cost = {baseRate};
        if (hours > 1) {
            cost[0] += (hours - 1) * additionalRate;
        }

        databaseReference.child("isReturningUser").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isReturningUser = snapshot.exists() && snapshot.getValue(Boolean.class);
                if (isReturningUser) {
                    cost[0] *= 0.8;
                }

                if (walletBalance >= cost[0]) {
                    Toast.makeText(Book_Parking.this, "Booking successful!", Toast.LENGTH_SHORT).show();
                    walletBalance -= cost[0];
                    databaseReference.child("wallet").setValue(walletBalance);

                    DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings");
                    String bookingId = bookingsRef.push().getKey();
                    Booking booking = new Booking(pincode, vehicleType, startTime, endTime, cost[0]);

                    if (bookingId != null) {
                        bookingsRef.child(bookingId).setValue(booking).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(Book_Parking.this, "Booking saved successfully!", Toast.LENGTH_SHORT).show();
                                databaseReference.child("isReturningUser").setValue(true);
                                finish();
                            } else {
                                Toast.makeText(Book_Parking.this, "Failed to save booking.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(Book_Parking.this, "Insufficient wallet balance.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Book_Parking.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class Booking {
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
