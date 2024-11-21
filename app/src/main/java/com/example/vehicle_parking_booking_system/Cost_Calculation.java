package com.example.vehicle_parking_booking_system;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Cost_Calculation extends AppCompatActivity {

    private RadioGroup rgVehicleType;
    private Button btnSelectStartTime, btnSelectEndTime;
    private TextView tvStartTime, tvEndTime, tvTotalCost;

    private String selectedStartTime, selectedEndTime;
    private int vehicleCostPerHourFirst, vehicleCostPerHourAdditional;
    private boolean isReturningUser = true; // Assume user data is known, set to `true` for 20% discount.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_calculation);

        // Initialize views
        rgVehicleType = findViewById(R.id.rgVehicleType);
        btnSelectStartTime = findViewById(R.id.btnSelectStartTime);
        btnSelectEndTime = findViewById(R.id.btnSelectEndTime);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvTotalCost = findViewById(R.id.tvTotalCost);

        // Set vehicle type listener
        rgVehicleType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCar) {
                vehicleCostPerHourFirst = 150; // Cost for first hour (car)
                vehicleCostPerHourAdditional = 80; // Cost for additional hours (car)
            } else if (checkedId == R.id.rbBike) {
                vehicleCostPerHourFirst = 50; // Cost for first hour (bike)
                vehicleCostPerHourAdditional = 30; // Cost for additional hours (bike)
            }
            calculateCost();
        });

        // Set time pickers
        btnSelectStartTime.setOnClickListener(v -> openTimePicker(true));
        btnSelectEndTime.setOnClickListener(v -> openTimePicker(false));
    }

    private void openTimePicker(boolean isStartTime) {
        final int hour = 0; // Default to midnight
        final int minute = 0;

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    if (isStartTime) {
                        selectedStartTime = time;
                        tvStartTime.setText("Start Time: " + time);
                    } else {
                        selectedEndTime = time;
                        tvEndTime.setText("End Time: " + time);
                    }
                    calculateCost();
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void calculateCost() {
        if (selectedStartTime == null || selectedEndTime == null || vehicleCostPerHourFirst == 0) {
            tvTotalCost.setText("Total Cost: 0 Rs");
            return;
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        try {
            Date startTime = timeFormat.parse(selectedStartTime);
            Date endTime = timeFormat.parse(selectedEndTime);

            if (startTime != null && endTime != null && !endTime.before(startTime)) {
                long diffInMillis = endTime.getTime() - startTime.getTime();
                long diffInMinutes = diffInMillis / (1000 * 60);
                long hours = diffInMinutes / 60;
                long remainingMinutes = diffInMinutes % 60;

                // Calculate total cost
                int totalCost;
                if (hours == 0 && remainingMinutes > 0) {
                    totalCost = vehicleCostPerHourFirst; // Charge only first-hour rate
                } else {
                    totalCost = vehicleCostPerHourFirst + (int) (hours * vehicleCostPerHourAdditional);
                }

                // Apply discount if applicable
                if (isReturningUser) {
                    totalCost = (int) (totalCost * 0.8); // Apply 20% discount
                }

                tvTotalCost.setText("Total Cost: " + totalCost + " Rs");
            } else {
                tvTotalCost.setText("Total Cost: 0 Rs");
                Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
