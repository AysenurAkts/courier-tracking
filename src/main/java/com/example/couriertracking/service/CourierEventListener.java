package com.example.couriertracking.service;

import com.example.couriertracking.model.EntranceLog;

public interface CourierEventListener {
    void onStoreEntered(EntranceLog entranceLog);
}
