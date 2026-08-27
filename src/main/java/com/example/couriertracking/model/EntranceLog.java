package com.example.couriertracking.model;

import java.time.Instant;

public record EntranceLog(String courierId, String storeName, Instant time, double distanceMeters) {
}
