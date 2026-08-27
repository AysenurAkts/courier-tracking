package com.example.couriertracking.service;

import com.example.couriertracking.model.EntranceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EntranceLogService implements CourierEventListener {
    private static final Logger log = LoggerFactory.getLogger(EntranceLogService.class);
    private final List<EntranceLog> logs = new CopyOnWriteArrayList<>();

    @Override
    public void onStoreEntered(EntranceLog entranceLog) {
        logs.add(entranceLog);
        log.info("Courier {} entered {} at {} ({} m)",
                entranceLog.courierId(), entranceLog.storeName(), entranceLog.time(),
                Math.round(entranceLog.distanceMeters()));
    }

    public List<EntranceLog> getLogs() {
        return List.copyOf(logs);
    }
}
