package com.example.vehicle_parking_booking_system;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<Booking> bookingList;
    private OnBookingClickListener listener;

    public BookingAdapter(List<Booking> bookingList, OnBookingClickListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.textViewLocation.setText(booking.getLocation());
        holder.textViewStartTime.setText("Start: " + booking.getStartTime());
        holder.textViewEndTime.setText("End: " + booking.getEndTime());
        holder.textViewParkingFee.setText("Fee: ₹" + booking.getParkingFee());
        holder.textViewUserName.setText("User: " + booking.getUserName());
        holder.textViewVehicleType.setText("Vehicle: " + booking.getVehicleType());

        // Set click listener for cancel button
        holder.btnCancelBooking.setOnClickListener(v -> listener.onBookingCancelClick(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLocation, textViewStartTime, textViewEndTime, textViewParkingFee, textViewUserName, textViewVehicleType;
        Button btnCancelBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewStartTime = itemView.findViewById(R.id.textViewStartTime);
            textViewEndTime = itemView.findViewById(R.id.textViewEndTime);
            textViewParkingFee = itemView.findViewById(R.id.textViewParkingFee);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewVehicleType = itemView.findViewById(R.id.textViewVehicleType);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
        }
    }

    public interface OnBookingClickListener {
        void onBookingCancelClick(Booking booking);
    }
}
