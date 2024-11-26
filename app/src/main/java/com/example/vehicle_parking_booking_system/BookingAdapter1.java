package com.example.vehicle_parking_booking_system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BookingAdapter1 extends ArrayAdapter<Booking1> {

    private Context context;
    private List<Booking1> bookings;

    public BookingAdapter1(Context context, List<Booking1> bookings) {
        super(context, R.layout.booking_item, bookings);
        this.context = context;
        this.bookings = bookings;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the booking object for the current position
        Booking1 booking = bookings.get(position);

        // Create a view for the booking item
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.booking_item, parent, false);
        }

        // Set the values for each field in the booking item layout
        TextView tvBookingId = convertView.findViewById(R.id.tvBookingId);
        TextView tvUserName = convertView.findViewById(R.id.tvUserName);
        TextView tvLocation = convertView.findViewById(R.id.tvLocation);
        TextView tvVehicleType = convertView.findViewById(R.id.tvVehicleType);
        TextView tvStartTime = convertView.findViewById(R.id.tvStartTime);
        TextView tvEndTime = convertView.findViewById(R.id.tvEndTime);
        TextView tvParkingFee = convertView.findViewById(R.id.tvParkingFee);
        TextView tvBookingDate = convertView.findViewById(R.id.tvBookingDate);

        // Set the text for each TextView
        tvBookingId.setText("Booking ID: " + booking.getBookingId());
        tvUserName.setText("User: " + booking.getUserName());
        tvLocation.setText("Location: " + booking.getLocation());
        tvVehicleType.setText("Vehicle: " + booking.getVehicleType());
        tvStartTime.setText("Start Time: " + booking.getStartTime());
        tvEndTime.setText("End Time: " + booking.getEndTime());
        tvParkingFee.setText("Fee: ₹" + booking.getParkingFee());
        tvBookingDate.setText("Date: " + booking.getDate());

        return convertView;
    }
}
