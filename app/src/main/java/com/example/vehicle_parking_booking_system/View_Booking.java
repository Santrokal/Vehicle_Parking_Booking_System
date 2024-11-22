package com.example.vehicle_parking_booking_system;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class View_Booking extends AppCompatActivity {

    private ListView listViewBookings;
    private List<Booking> bookingList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference bookingReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_booking);

        // Initialize UI components
        listViewBookings = findViewById(R.id.lvBookings);
        bookingList = new ArrayList<>();

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        bookingReference = FirebaseDatabase.getInstance().getReference("Bookings");

        // Fetch bookings from the database
        fetchBookings(userId);
    }

    private void fetchBookings(String userId) {
        bookingReference.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear(); // Clear the list before adding new data
                if (snapshot.exists()) {
                    for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                        Booking booking = bookingSnapshot.getValue(Booking.class);
                        if (booking != null) {
                            bookingList.add(booking);
                        }
                    }
                    BookingAdapter adapter = new BookingAdapter(View_Booking.this, bookingList);
                    listViewBookings.setAdapter(adapter);
                } else {
                    Toast.makeText(View_Booking.this, "No bookings found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(View_Booking.this, "Failed to fetch bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class BookingAdapter extends android.widget.ArrayAdapter<Booking> {

        private final List<Booking> bookings;

        public BookingAdapter(@NonNull AppCompatActivity context, @NonNull List<Booking> bookings) {
            super(context, R.layout.booking_item, bookings);
            this.bookings = bookings;
        }

        @NonNull
        @Override
        public android.view.View getView(int position, @NonNull android.view.View convertView, @NonNull android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = android.view.LayoutInflater.from(getContext()).inflate(R.layout.booking_item, parent, false);
            }

            Booking booking = bookings.get(position);

            // Populate booking data into the UI
            android.widget.TextView tvBookingId = convertView.findViewById(R.id.tvBookingId);
            android.widget.TextView tvLocation = convertView.findViewById(R.id.tvLocation);
            android.widget.TextView tvVehicleType = convertView.findViewById(R.id.tvVehicleType);
            android.widget.TextView tvStartTime = convertView.findViewById(R.id.tvStartTime);
            android.widget.TextView tvEndTime = convertView.findViewById(R.id.tvEndTime);
            android.widget.TextView tvParkingFee = convertView.findViewById(R.id.tvParkingFee);
            android.widget.TextView tvDate = convertView.findViewById(R.id.tvDate);

            tvBookingId.setText("Booking ID: " + booking.getBookingId());
            tvLocation.setText("Location: " + booking.getLocation());
            tvVehicleType.setText("Vehicle Type: " + booking.getVehicleType());
            tvStartTime.setText("Start Time: " + booking.getStartTime());
            tvEndTime.setText("End Time: " + booking.getEndTime());
            tvParkingFee.setText("Fee: ₹" + booking.getParkingFee());
            tvDate.setText("Date: " + booking.getDate()); // Set the date field

            return convertView;
        }
    }

    public static class Booking {
        private String bookingId;
        private String userId;
        private String location;
        private String vehicleType;
        private String startTime;
        private String endTime;
        private double parkingFee;
        private String userName;
        private String date; // Added field for the booking date

        public Booking() {
        }

        public Booking(String bookingId, String userId, String location, String vehicleType, String startTime, String endTime, double parkingFee, String userName, String date) {
            this.bookingId = bookingId;
            this.userId = userId;
            this.location = location;
            this.vehicleType = vehicleType;
            this.startTime = startTime;
            this.endTime = endTime;
            this.parkingFee = parkingFee;
            this.userName = userName;
            this.date = date;
        }

        public String getBookingId() {
            return bookingId;
        }

        public String getUserId() {
            return userId;
        }

        public String getLocation() {
            return location;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public double getParkingFee() {
            return parkingFee;
        }

        public String getUserName() {
            return userName;
        }

        public String getDate() {
            return date;
        }
    }
}
