package com.example.couriertracking.model;

import java.time.Instant;

public record CourierLocation(String courierId, Instant time, double lat, double lng) {
}
