package com.example.couriertracking.dto;

import java.time.Instant;

public record LocationResponse(String courierId, Instant time, double lat, double lng) {
}
