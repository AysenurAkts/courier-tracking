package com.example.couriertracking.controller;

import com.example.couriertracking.dto.LocationRequest;
import com.example.couriertracking.dto.LocationResponse;
import com.example.couriertracking.model.CourierLocation;
import com.example.couriertracking.model.EntranceLog;
import com.example.couriertracking.service.EntranceLogService;
import com.example.couriertracking.service.TrackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CourierController {
    private final TrackingService trackingService;
    private final EntranceLogService entranceLogService;

    public CourierController(TrackingService trackingService, EntranceLogService entranceLogService) {
        this.trackingService = trackingService;
        this.entranceLogService = entranceLogService;
    }

    @PostMapping("/locations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public LocationResponse recordLocation(@Valid @RequestBody LocationRequest request) {
        CourierLocation location = trackingService.record(
                new CourierLocation(request.courierId(), request.time(), request.lat(), request.lng()));
        return new LocationResponse(location.courierId(), location.time(), location.lat(), location.lng());
    }

    @GetMapping("/couriers/{courierId}/distance")
    public double getTotalTravelDistance(@PathVariable String courierId) {
        return trackingService.getTotalTravelDistance(courierId);
    }

    @GetMapping("/entrances")
    public List<EntranceLog> getEntranceLogs() {
        return entranceLogService.getLogs();
    }
}
