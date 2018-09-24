package ru.spbstu.ioffe.satellite;

public class LLA {
    private double latitude;
    private double longitude;
    private double altitude;

    public LLA(double latitude, double longitude, double altitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getAltitude() {
        return altitude;
    }
}
