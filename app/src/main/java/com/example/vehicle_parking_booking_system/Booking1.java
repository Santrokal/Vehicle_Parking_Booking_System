package com.example.vehicle_parking_booking_system;

public class Booking1 {

    private String bookingId;
    private String userId;
    private String location;
    private String vehicleType;
    private String startTime;
    private String endTime;
    private double parkingFee;
    private String userName;
    private String date; // Added date field to store booking date

    public Booking1() {
        // Default constructor required for Firebase
    }

    public Booking1(String bookingId, String userId, String location, String vehicleType,
                    String startTime, String endTime, double parkingFee,
                    String userName, String date) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.location = location;
        this.vehicleType = vehicleType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.parkingFee = parkingFee;
        this.userName = userName;
        this.date = date; // Initialize the date
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getParkingFee() {
        return parkingFee;
    }

    public void setParkingFee(double parkingFee) {
        this.parkingFee = parkingFee;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
