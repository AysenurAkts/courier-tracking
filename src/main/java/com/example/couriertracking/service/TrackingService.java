package com.example.couriertracking.service;

import com.example.couriertracking.model.CourierLocation;
import com.example.couriertracking.model.EntranceLog;
import com.example.couriertracking.model.Store;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrackingService {
    private static final double STORE_RADIUS_METERS = 100;
    private static final Duration REENTRY_COOLDOWN = Duration.ofMinutes(1);

    private final DistanceCalculator distanceCalculator;
    private final StoreCatalog storeCatalog;
    private final List<CourierEventListener> listeners;
    private final Map<String, CourierLocation> lastLocations = new ConcurrentHashMap<>();
    private final Map<String, Double> totalDistances = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Instant>> lastEntrances = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Boolean>> insideStores = new ConcurrentHashMap<>();

    public TrackingService(DistanceCalculator distanceCalculator, StoreCatalog storeCatalog,
                           List<CourierEventListener> listeners) {
        this.distanceCalculator = distanceCalculator;
        this.storeCatalog = storeCatalog;
        this.listeners = List.copyOf(listeners);
    }

    public synchronized CourierLocation record(CourierLocation location) {
        validateCoordinates(location);
        CourierLocation previous = lastLocations.get(location.courierId());
        if (previous != null && location.time().isBefore(previous.time())) {
            throw new IllegalArgumentException("Location time cannot be before the courier's last location");
        }
        if (previous != null) {
            totalDistances.merge(location.courierId(),
                    distanceCalculator.calculateMeters(previous, location), Double::sum);
        }
        lastLocations.put(location.courierId(), location);
        detectEntrances(location);
        return location;
    }

    public double getTotalTravelDistance(String courierId) {
        return totalDistances.getOrDefault(courierId, 0.0);
    }

    private void detectEntrances(CourierLocation location) {
        Map<String, Instant> courierEntrances =
                lastEntrances.computeIfAbsent(location.courierId(), ignored -> new ConcurrentHashMap<>());
        Map<String, Boolean> courierStoreStates =
                insideStores.computeIfAbsent(location.courierId(), ignored -> new ConcurrentHashMap<>());
        for (Store store : storeCatalog.getStores()) {
            double distance = distanceCalculator.calculateMeters(
                    location, new CourierLocation(store.name(), location.time(), store.lat(), store.lng()));
            boolean currentlyInside = distance <= STORE_RADIUS_METERS;
            boolean previouslyInside = courierStoreStates.getOrDefault(store.name(), false);
            courierStoreStates.put(store.name(), currentlyInside);
            if (currentlyInside && !previouslyInside) {
                Instant lastEntrance = courierEntrances.get(store.name());
                if (lastEntrance == null || Duration.between(lastEntrance, location.time()).compareTo(REENTRY_COOLDOWN) >= 0) {
                    courierEntrances.put(store.name(), location.time());
                    listeners.forEach(listener -> listener.onStoreEntered(
                            new EntranceLog(location.courierId(), store.name(), location.time(), distance)));
                }
            }
        }
    }

    private void validateCoordinates(CourierLocation location) {
        if (location.lat() < -90 || location.lat() > 90 || location.lng() < -180 || location.lng() > 180) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 and longitude between -180 and 180");
        }
    }
}
