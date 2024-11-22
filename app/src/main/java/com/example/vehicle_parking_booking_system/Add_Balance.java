
package com.example.vehicle_parking_booking_system;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.Map;

public class Add_Balance extends AppCompatActivity implements PaymentResultListener {

    private EditText etAmount;
    private Button btnRecharge;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userReference;

    private static final String TAG = "Add_Balance";

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
        userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

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
            Log.e(TAG, "Error in payment: " + e.getMessage());
            Toast.makeText(this, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Log.d(TAG, "Payment Successful: " + razorpayPaymentID);
        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();

        String amountText = etAmount.getText().toString().trim();
        double amount = Double.parseDouble(amountText);

        userReference.child("walletBalance").get().addOnCompleteListener(walletTask -> {
            if (walletTask.isSuccessful()) {
                Double currentBalance = walletTask.getResult().getValue(Double.class);
                if (currentBalance == null) {
                    currentBalance = 0.0; // Initialize if walletBalance doesn't exist
                }

                double updatedBalance = currentBalance + amount;

                // Update walletBalance and add recharge details in one operation
                userReference.updateChildren(
                        Map.of(
                                "walletBalance", updatedBalance,
                                "lastTransaction", razorpayPaymentID
                        )
                ).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Log.d(TAG, "Wallet balance updated successfully to: " + updatedBalance);

                        // Add recharge details
                        String rechargeId = userReference.child("Recharges").push().getKey();
                        if (rechargeId != null) {
                            DatabaseReference rechargeRef = userReference.child("Recharges").child(rechargeId);
                            rechargeRef.setValue(
                                    Map.of(
                                            "amount", amount,
                                            "transactionId", razorpayPaymentID,
                                            "timestamp", System.currentTimeMillis(),
                                            "status", "Completed"
                                    )
                            );
                        }

                        // Navigate to PaymentSuccessActivity
                        Intent intent = new Intent(Add_Balance.this, Payment_Success.class);
                        intent.putExtra("paymentId", razorpayPaymentID);
                        intent.putExtra("modeOfPayment", "Razorpay");
                        intent.putExtra("amount", amount);
                        intent.putExtra("userEmail", firebaseAuth.getCurrentUser().getEmail());
                        startActivity(intent);
                    } else {
                        Log.e(TAG, "Failed to update wallet balance.");
                        Toast.makeText(this, "Failed to update wallet balance.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e(TAG, "Failed to fetch wallet balance: " + walletTask.getException());
                Toast.makeText(this, "Failed to fetch wallet balance.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onPaymentError(int code, String response) {
        Log.e(TAG, "Payment failed: " + response);
        Toast.makeText(this, "Payment failed: " + response, Toast.LENGTH_SHORT).show();
    }
}
