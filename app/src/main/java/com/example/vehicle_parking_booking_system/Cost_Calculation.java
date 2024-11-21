package com.example.vehicle_parking_booking_system;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Cost_Calculation extends AppCompatActivity {

    private RadioGroup rgVehicleType;
    private Button btnSelectStartDate, btnSelectEndDate;
    private TextView tvStartDate, tvEndDate, tvTotalCost;

    private String selectedStartDate, selectedEndDate;
    private int vehicleCost = 0; // Cost per day for selected vehicle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_calculation);

        // Initialize views
        rgVehicleType = findViewById(R.id.rgVehicleType);
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvTotalCost = findViewById(R.id.tvTotalCost);

        // Set vehicle type listener
        rgVehicleType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCar) {
                vehicleCost = 150; // Cost for car
            } else if (checkedId == R.id.rbBike) {
                vehicleCost = 50; // Cost for bike
            }
            calculateCost();
        });

        // Set date pickers
        btnSelectStartDate.setOnClickListener(v -> openDatePicker(true));
        btnSelectEndDate.setOnClickListener(v -> openDatePicker(false));
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
                        selectedStartDate = date;
                        tvStartDate.setText("Start Date: " + date);
                    } else {
                        selectedEndDate = date;
                        tvEndDate.setText("End Date: " + date);
                    }
                    calculateCost();
                }, year, month, day);
        datePickerDialog.show();
    }

    private void calculateCost() {
        if (selectedStartDate == null || selectedEndDate == null || vehicleCost == 0) {
            tvTotalCost.setText("Total Cost: 0 Rs");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date startDate = dateFormat.parse(selectedStartDate);
            Date endDate = dateFormat.parse(selectedEndDate);

            if (startDate != null && endDate != null && !endDate.before(startDate)) {
                long diffInMillis = endDate.getTime() - startDate.getTime();
                long days = (diffInMillis / (1000 * 60 * 60 * 24)) + 1; // Including the start day
                int totalCost = (int) days * vehicleCost;
                tvTotalCost.setText("Total Cost: " + totalCost + " Rs");
            } else {
                tvTotalCost.setText("Total Cost: 0 Rs");
                Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}