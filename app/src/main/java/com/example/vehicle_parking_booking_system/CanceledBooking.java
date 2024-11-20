package com.example.vehicle_parking_booking_system;


public class CanceledBooking {
    private String bookingId, location, canceledDateTime, name, email, phone;
    private double refundedAmount;

    public CanceledBooking(String bookingId, String location, String canceledDateTime, double refundedAmount, String name, String email, String phone) {
        this.bookingId = bookingId;
        this.location = location;
        this.canceledDateTime = canceledDateTime;
        this.refundedAmount = refundedAmount;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Default constructor for Firebase
    public CanceledBooking() {
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public String getLocation() {
        return location;
    }

    public String getCanceledDateTime() {
        return canceledDateTime;
    }

    public double getRefundedAmount() {
        return refundedAmount;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
