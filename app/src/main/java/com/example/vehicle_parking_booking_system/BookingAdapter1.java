package com.example.vehicle_parking_booking_system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingAdapter1 extends RecyclerView.Adapter<BookingAdapter1.BookingViewHolder> {

    private Context context;
    private List<Booking1> bookingList;

    public BookingAdapter1(Context context, List<Booking1> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.booking_item, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking1 booking = bookingList.get(position);

        holder.tvLocation.setText("Location: " + booking.getLocation());
        holder.tvVehicleType.setText("Vehicle: " + booking.getVehicleType());
        holder.tvStartTime.setText("Start Time: " + booking.getStartTime());
        holder.tvEndTime.setText("End Time: " + booking.getEndTime());
        holder.tvParkingFee.setText("Fee: ₹" + booking.getParkingFee());
        holder.tvDate.setText("Date: " + booking.getDate());
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {

        TextView tvLocation, tvVehicleType, tvStartTime, tvEndTime, tvParkingFee, tvDate;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            tvParkingFee = itemView.findViewById(R.id.tvParkingFee);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
