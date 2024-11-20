package com.example.vehicle_parking_booking_system;


public class Booking {
    private String bookingId;
    private String location;
    private String startTime;
    private String endTime;
    private double parkingFee;
    private String userName;
    private String vehicleType;

    public Booking(String bookingId, String location, String startTime, String endTime, double parkingFee, String userName, String vehicleType) {
        this.bookingId = bookingId;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.parkingFee = parkingFee;
        this.userName = userName;
        this.vehicleType = vehicleType;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getLocation() {
        return location;
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

    public String getVehicleType() {
        return vehicleType;
    }
}
