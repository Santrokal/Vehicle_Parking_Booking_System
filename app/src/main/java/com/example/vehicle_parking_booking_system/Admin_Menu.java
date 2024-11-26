package com.example.vehicle_parking_booking_system;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Admin_Menu extends AppCompatActivity {

    private TextView tvWelcome, tvDateTime;
    private Button btnViewBooking, btnViewFeedback, btnViewUsers,btnlogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);

        // Initialize UI elements
        tvWelcome = findViewById(R.id.tvWelcome);
        tvDateTime = findViewById(R.id.tvDateTime);
        btnViewBooking = findViewById(R.id.btnViewBooking);
        btnViewFeedback = findViewById(R.id.btnViewFeedback);
        btnViewUsers = findViewById(R.id.btnViewUsers);
        btnlogout = findViewById(R.id.btnlogout);


        // Set current date and time
        String currentDateTime = new SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(new Date());
        tvDateTime.setText(currentDateTime);

        // Set button click listeners
        btnViewBooking.setOnClickListener(v -> {
            // Navigate to View Booking Activity
            Intent intent = new Intent(Admin_Menu.this, View_Booking.class);
            startActivity(intent);
        });

        btnViewFeedback.setOnClickListener(v -> {
            // Navigate to View Feedback Activity
            Intent intent = new Intent(Admin_Menu.this, View_Feedback.class);
            startActivity(intent);
        });

        btnViewUsers.setOnClickListener(v -> {
            // Navigate to View Users Activity
            Intent intent = new Intent(Admin_Menu.this, View_Users.class);
            startActivity(intent);
        });
        btnlogout.setOnClickListener(v -> {
            // Navigate to View Users Activity
            Intent intent = new Intent(Admin_Menu.this, user_login.class);
            startActivity(intent);
        });
    }
}
