package com.example.couriertracking;

import com.example.couriertracking.model.CourierLocation;
import com.example.couriertracking.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackingServiceTest {
    @Test
    void sumsSequentialTravelAndIgnoresReentryWithinOneMinute() {
        StoreCatalog catalog = new StoreCatalog(new ObjectMapper());
        assertDoesNotThrow(() -> catalog.loadStores());
        EntranceLogService logger = new EntranceLogService();
        TrackingService service = new TrackingService(new HaversineDistanceCalculator(), catalog, List.of(logger));
        Instant t = Instant.parse("2026-01-01T10:00:00Z");
        service.record(new CourierLocation("c1", t, 40.9923307, 29.1244229));
        service.record(new CourierLocation("c1", t.plusSeconds(30), 40.9923307, 29.1244229));
        service.record(new CourierLocation("c1", t.plusSeconds(61), 40.9935, 29.1244229));
        service.record(new CourierLocation("c1", t.plusSeconds(122), 40.9923307, 29.1244229));
        assertEquals(2, logger.getLogs().size());
        assertEquals(0.0, service.getTotalTravelDistance("unknown"));
    }

    @Test
    void rejectsInvalidCoordinates() {
        StoreCatalog catalog = new StoreCatalog(new ObjectMapper());
        assertDoesNotThrow(() -> catalog.loadStores());
        TrackingService service = new TrackingService(
                new HaversineDistanceCalculator(), catalog, List.of());

        CourierLocation invalidLocation = new CourierLocation(
                "c1", Instant.parse("2026-01-01T10:00:00Z"), 91.0, 29.1244229);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.record(invalidLocation));

        assertEquals(
                "Latitude must be between -90 and 90 and longitude between -180 and 180",
                exception.getMessage());
    }

    @Test
    void rejectsLocationWithEarlierTimestamp() {
        StoreCatalog catalog = new StoreCatalog(new ObjectMapper());
        assertDoesNotThrow(() -> catalog.loadStores());
        TrackingService service = new TrackingService(
                new HaversineDistanceCalculator(), catalog, List.of());
        service.record(new CourierLocation(
                "c1", Instant.parse("2026-01-01T10:00:00Z"), 40.99, 29.12));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.record(new CourierLocation(
                        "c1", Instant.parse("2026-01-01T09:59:59Z"), 40.99, 29.12)));

        assertEquals(
                "Location time cannot be before the courier's last location",
                exception.getMessage());
    }
}
