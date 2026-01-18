package com.smartylighting.streetlights.service;

import com.smartylighting.streetlights.model.command.DimLightCommand;
import com.smartylighting.streetlights.model.command.TurnOnOffCommand;
import com.smartylighting.streetlights.model.event.LightMeasuredEvent;
import com.smartylighting.streetlights.producer.LightMeasurementProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StreetlightService {

    private static final Logger log = LoggerFactory.getLogger(StreetlightService.class);
    private final LightMeasurementProducer lightMeasurementProducer;

    // in-memory storage of streetlight states
    // in prod, this would be a DB
    private final Map<String, StreetlightState> streetlights = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Autowired
    public StreetlightService(LightMeasurementProducer lightMeasurementProducer) {
        this.lightMeasurementProducer = lightMeasurementProducer;
    }

    public void turnOn(String streetlightId, TurnOnOffCommand command) {
        log.info("Processing TURN ON command for streetlight: {}", streetlightId);

        StreetlightState state = getOrCreateState(streetlightId);
        boolean isNewState = state.getLastCommandTime() == null;

        state.setOn(true);
        state.setLastCommandTime(command.getSentAt());

        streetlights.put(streetlightId, state);

        log.info("Streetlight {} is now ON", streetlightId);

        if (isNewState) {
            log.warn("Skipping light measurement publish for newly created state: {}", streetlightId);
            return;
        }

        // Publish light measurement event after turning on
        publishLightMeasurement(streetlightId, state);
    }

    public void turnOff(String streetlightId, TurnOnOffCommand command) {
        log.info("Processing TURN OFF command for streetlight: {}", streetlightId);

        StreetlightState state = getOrCreateState(streetlightId);
        state.setOn(false);
        state.setLastCommandTime(command.getSentAt());

        streetlights.put(streetlightId, state);

        log.info("Streetlight {} is now OFF", streetlightId);

        // publish light measurement event showing light is off
        publishLightMeasurement(streetlightId, state);
    }

    public void dim(String streetlightId, DimLightCommand command) {
        log.info("Processing DIM command for streetlight {} to {}%", streetlightId, command.getPercentage());

        StreetlightState state = getOrCreateState(streetlightId);
        state.setDimPercentage(command.getPercentage());
        state.setLastCommandTime(command.getSentAt());

        streetlights.put(streetlightId, state);

        log.info("Streetlight {} dimmed to {}%", streetlightId, command.getPercentage());

        // publish new measurement with dimmed value
        publishLightMeasurement(streetlightId, state);
    }

    public void measureLight(String streetlightId) {
        log.info("Manually triggering light measurement for streetlight: {}", streetlightId);
        StreetlightState state = getOrCreateState(streetlightId);
        publishLightMeasurement(streetlightId, state);
    }

    private void publishLightMeasurement(String streetlightId, StreetlightState state) {
        int lumens = calculateLumens(state);

        LightMeasuredEvent event = new LightMeasuredEvent(
                lumens,
                LocalDateTime.now(),
                random.nextInt(101) // Random header value 0-100 as per AsyncAPI spec
        );

        log.debug("Publishing light measurement: {} lumens for streetlight {}", lumens, streetlightId);

        lightMeasurementProducer.publishLightMeasurement(streetlightId, event);
    }

    private int calculateLumens(StreetlightState state) {
        if (!state.isOn()) {
            return 0;
        }

        // base lumens when fully ON
        int baseLumens = 5000;

        // dimming %
        return (baseLumens * state.getDimPercentage()) / 100;
    }

    private StreetlightState getOrCreateState(String streetlightId) {
        return streetlights.computeIfAbsent(streetlightId, id -> {
            log.info("Creating new streetlight state for ID: {}", id);
            StreetlightState state = new StreetlightState();
            state.setStreetlightId(id);
            state.setOn(false);
            state.setDimPercentage(100); // default full brightness
            return state;
        });
    }

    public StreetlightState getState(String streetlightId) {
        return streetlights.get(streetlightId);
    }

    public Map<String, StreetlightState> getAllStates() {
        return new ConcurrentHashMap<>(streetlights);
    }

    public static class StreetlightState {
        private String streetlightId;
        private boolean isOn = false;
        private int dimPercentage = 100;
        private LocalDateTime lastCommandTime;

        public String getStreetlightId() {
            return streetlightId;
        }

        public void setStreetlightId(String streetlightId) {
            this.streetlightId = streetlightId;
        }

        public boolean isOn() {
            return isOn;
        }

        public void setOn(boolean on) {
            isOn = on;
        }

        public int getDimPercentage() {
            return dimPercentage;
        }

        public void setDimPercentage(int dimPercentage) {
            this.dimPercentage = dimPercentage;
        }

        public LocalDateTime getLastCommandTime() {
            return lastCommandTime;
        }

        public void setLastCommandTime(LocalDateTime lastCommandTime) {
            this.lastCommandTime = lastCommandTime;
        }

        @Override
        public String toString() {
            return "StreetlightState{" +
                    "streetlightId='" + streetlightId + '\'' +
                    ", isOn=" + isOn +
                    ", dimPercentage=" + dimPercentage +
                    ", lastCommandTime=" + lastCommandTime +
                    '}';
        }
    }
}