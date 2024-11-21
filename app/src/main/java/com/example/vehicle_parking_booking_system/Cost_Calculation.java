package com.example.vehicle_parking_booking_system;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Cost_Calculation extends AppCompatActivity {

    private RadioGroup rgVehicleType;
    private Button btnSelectStartDate, btnSelectEndDate, btnSelectStartTime, btnSelectEndTime;
    private TextView tvStartDateTime, tvEndDateTime, tvTotalCost;

    private String selectedStartDateTime, selectedEndDateTime;
    private int vehicleCostPerHourFirst, vehicleCostPerHourAdditional;
    private boolean isReturningUser = true; // Assume user data is known, set to `true` for 20% discount.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_calculation);

        // Initialize views
        rgVehicleType = findViewById(R.id.rgVehicleType);
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        btnSelectStartTime = findViewById(R.id.btnSelectStartTime);
        btnSelectEndTime = findViewById(R.id.btnSelectEndTime);
        tvStartDateTime = findViewById(R.id.tvStartDateTime);
        tvEndDateTime = findViewById(R.id.tvEndDateTime);
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

        // Set date and time pickers
        btnSelectStartDate.setOnClickListener(v -> openDatePicker(true));
        btnSelectEndDate.setOnClickListener(v -> openDatePicker(false));
        btnSelectStartTime.setOnClickListener(v -> openTimePicker(true));
        btnSelectEndTime.setOnClickListener(v -> openTimePicker(false));
    }

    private void openDatePicker(boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    if (isStartDate) {
                        selectedStartDateTime = date; // Set only date initially
                        tvStartDateTime.setText("Start Date: " + date);
                    } else {
                        selectedEndDateTime = date; // Set only date initially
                        tvEndDateTime.setText("End Date: " + date);
                    }
                    calculateCost();
                }, year, month, day);
        datePickerDialog.show();
    }

    private void openTimePicker(boolean isStartTime) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    if (isStartTime) {
                        selectedStartDateTime += " " + time; // Append time to selected start date
                        tvStartDateTime.setText("Start DateTime: " + selectedStartDateTime);
                    } else {
                        selectedEndDateTime += " " + time; // Append time to selected end date
                        tvEndDateTime.setText("End DateTime: " + selectedEndDateTime);
                    }
                    calculateCost();
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void calculateCost() {
        if (selectedStartDateTime == null || selectedEndDateTime == null || vehicleCostPerHourFirst == 0) {
            tvTotalCost.setText("Total Cost: 0 Rs");
            return;
        }

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date startDateTime = dateTimeFormat.parse(selectedStartDateTime);
            Date endDateTime = dateTimeFormat.parse(selectedEndDateTime);

            if (startDateTime != null && endDateTime != null && !endDateTime.before(startDateTime)) {
                long diffInMillis = endDateTime.getTime() - startDateTime.getTime();
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
                Toast.makeText(this, "End date and time must be after start date and time", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
