package com.example.vehicle_parking_booking_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Success_Booking extends AppCompatActivity {

    private TextView tvBookingId, tvLocation, tvStartTime, tvEndTime, tvParkingFee;
    private Button btnOkay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_booking);

        // Initialize views
        tvBookingId = findViewById(R.id.tvBookingId);
        tvLocation = findViewById(R.id.tvLocation);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvParkingFee = findViewById(R.id.tvParkingFee);
        btnOkay = findViewById(R.id.btnOkay);

        // Get booking details from intent
        Intent intent = getIntent();
        String bookingId = intent.getStringExtra("bookingId");
        String location = intent.getStringExtra("location");
        String startTime = intent.getStringExtra("startTime");
        String endTime = intent.getStringExtra("endTime");
        double parkingFee = intent.getDoubleExtra("parkingFee", 0);

        // Set booking details to views
        tvBookingId.setText("Booking ID: " + bookingId);
        tvLocation.setText("Location: " + location);
        tvStartTime.setText("Start Time: " + startTime);
        tvEndTime.setText("End Time: " + endTime);
        tvParkingFee.setText("Parking Fee: ₹" + parkingFee);

        // Handle Okay button click
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
