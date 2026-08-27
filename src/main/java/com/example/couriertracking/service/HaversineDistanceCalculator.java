package com.example.couriertracking.service;

import com.example.couriertracking.model.CourierLocation;
import org.springframework.stereotype.Component;

@Component
public class HaversineDistanceCalculator implements DistanceCalculator {
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    @Override
    public double calculateMeters(CourierLocation first, CourierLocation second) {
        double lat1 = Math.toRadians(first.lat());
        double lat2 = Math.toRadians(second.lat());
        double deltaLat = Math.toRadians(second.lat() - first.lat());
        double deltaLng = Math.toRadians(second.lng() - first.lng());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        a = Math.min(1.0, Math.max(0.0, a));
        return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
