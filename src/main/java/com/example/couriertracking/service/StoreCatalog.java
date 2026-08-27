package com.example.couriertracking.service;

import com.example.couriertracking.model.Store;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class StoreCatalog {
    private final ObjectMapper objectMapper;
    private List<Store> stores = List.of();

    public StoreCatalog(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadStores() throws IOException {
        try (var inputStream = new ClassPathResource("stores.json").getInputStream()) {
            stores = List.copyOf(objectMapper.readValue(
                    inputStream, new TypeReference<List<Store>>() {}));
        }
    }

    public List<Store> getStores() {
        return stores;
    }
}
