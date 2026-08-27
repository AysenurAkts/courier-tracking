package com.example.couriertracking.service;

import com.example.couriertracking.model.CourierLocation;

public interface DistanceCalculator {
    double calculateMeters(CourierLocation first, CourierLocation second);
}
