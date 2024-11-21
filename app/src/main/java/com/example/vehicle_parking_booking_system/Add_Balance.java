package com.example.vehicle_parking_booking_system;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

public class Add_Balance extends AppCompatActivity implements PaymentResultListener {

    private EditText etAmount;
    private Button btnRecharge;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_balance);

        // Initialize UI
        etAmount = findViewById(R.id.etAmount);
        btnRecharge = findViewById(R.id.btnRecharge);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Initialize Razorpay Checkout
        Checkout.preload(getApplicationContext());

        // Recharge Button Click
        btnRecharge.setOnClickListener(v -> startPayment());
    }

    private void startPayment() {
        String amountText = etAmount.getText().toString().trim();

        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter an amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountText) * 100; // Razorpay expects amount in paise

        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_9c7QAVv42X6z92"); // Replace with your Razorpay API Key

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Parking Booking System");
            options.put("description", "Wallet Recharge");
            options.put("currency", "INR");
            options.put("amount", amount);

            // Start Razorpay Checkout
            checkout.open(Add_Balance.this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();

        String userId = firebaseAuth.getCurrentUser().getUid();
        String rechargeId = databaseReference.child("Recharges").push().getKey(); // Generate unique key for this recharge

        String amountText = etAmount.getText().toString().trim();
        double amount = Double.parseDouble(amountText);

        // Fetch the user's name from the Realtime Database
        databaseReference.child("name").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String userName = task.getResult().getValue(String.class);

                // Update Wallet Balance
                databaseReference.child("wallet").get().addOnCompleteListener(walletTask -> {
                    if (walletTask.isSuccessful()) {
                        Double currentBalance = walletTask.getResult().getValue(Double.class);
                        if (currentBalance == null) currentBalance = 0.0;
                        double updatedBalance = currentBalance + amount;

                        // Update wallet balance
                        databaseReference.child("wallet").setValue(updatedBalance);

                        // Log the recharge details in the Recharges node
                        if (rechargeId != null) {
                            DatabaseReference rechargeRef = databaseReference.child("Recharges").child(rechargeId);

                            // Store the recharge data
                            rechargeRef.child("amount").setValue(amount);
                            rechargeRef.child("transactionId").setValue(razorpayPaymentID);
                            rechargeRef.child("timestamp").setValue(System.currentTimeMillis());  // Store timestamp (optional)
                            rechargeRef.child("userName").setValue(userName); // Use the fetched name
                            rechargeRef.child("status").setValue("Completed");  // Status of the recharge
                        }

                        // Pass data to PaymentSuccessActivity
                        Intent intent = new Intent(Add_Balance.this, Payment_Success.class);
                        intent.putExtra("paymentId", razorpayPaymentID);
                        intent.putExtra("modeOfPayment", "Razorpay"); // Assuming Razorpay as the mode
                        intent.putExtra("userName", userName); // Use fetched user name
                        intent.putExtra("userEmail", firebaseAuth.getCurrentUser().getEmail());
                        startActivity(intent);

                        Toast.makeText(this, "Recharge completed successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }




    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment failed: " + response, Toast.LENGTH_SHORT).show();
    }
}
