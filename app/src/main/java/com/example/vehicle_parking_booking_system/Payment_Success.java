package com.example.vehicle_parking_booking_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Payment_Success extends AppCompatActivity {

    private TextView tvPaymentId, tvModeOfPayment, tvUserName, tvUserEmail;
    private Button btnOkay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        // Initialize TextViews
        tvPaymentId = findViewById(R.id.tvPaymentId);
        tvModeOfPayment = findViewById(R.id.tvModeOfPayment);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnOkay = findViewById(R.id.btnOkay);

        // Get the intent that started this activity
        String paymentId = getIntent().getStringExtra("paymentId");
        String modeOfPayment = getIntent().getStringExtra("modeOfPayment");
        String userName = getIntent().getStringExtra("userName");
        String userEmail = getIntent().getStringExtra("userEmail");

        // Set the values in the TextViews
        tvPaymentId.setText("Payment ID: " + paymentId);
        tvModeOfPayment.setText("Mode of Payment: " + modeOfPayment);
        tvUserName.setText("Name: " + userName);
        tvUserEmail.setText("Email: " + userEmail);

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
