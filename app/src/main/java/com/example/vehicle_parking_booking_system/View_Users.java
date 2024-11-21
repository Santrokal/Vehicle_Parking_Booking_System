package com.example.vehicle_parking_booking_system;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class View_Users extends AppCompatActivity {

    private ListView lvUsers;
    private DatabaseReference usersDatabaseRef;
    private List<String> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_users);

        lvUsers = findViewById(R.id.lvUsers);

        // Initialize Firebase Database Reference
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize user list
        userList = new ArrayList<>();

        // Fetch users from Firebase
        fetchUsersFromDatabase();
    }

    private void fetchUsersFromDatabase() {
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear(); // Clear previous data
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Extract user details
                    String name = userSnapshot.child("name").getValue(String.class);
                    String email = userSnapshot.child("email").getValue(String.class);
                    String phone = userSnapshot.child("phone").getValue(String.class);
                    Double walletBalance = userSnapshot.child("wallet").getValue(Double.class);
                    Double totalRecharge = calculateTotalRecharge(userSnapshot.child("Recharges"));

                    if (name == null || email == null || phone == null) continue; // Skip incomplete data

                    // Format user data for display
                    String userInfo = "Name: " + name + "\n"
                            + "Email: " + email + "\n"
                            + "Phone: " + phone + "\n"
                            + "Wallet Balance: ₹" + (walletBalance != null ? walletBalance : 0.0) + "\n"
                            + "Total Recharge: ₹" + totalRecharge;

                    userList.add(userInfo);
                }

                // Display user data in ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(View_Users.this,
                        android.R.layout.simple_list_item_1, userList);
                lvUsers.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_Users.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Calculate total recharge amount from Recharges node
    private Double calculateTotalRecharge(DataSnapshot rechargesSnapshot) {
        double total = 0.0;
        for (DataSnapshot recharge : rechargesSnapshot.getChildren()) {
            Double amount = recharge.child("amount").getValue(Double.class);
            if (amount != null) {
                total += amount;
            }
        }
        return total;
    }
}
