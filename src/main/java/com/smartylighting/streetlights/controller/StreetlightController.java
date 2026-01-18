package com.smartylighting.streetlights.controller;

import com.smartylighting.streetlights.model.command.DimLightCommand;
import com.smartylighting.streetlights.model.command.TurnOnOffCommand;
import com.smartylighting.streetlights.service.StreetlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/streetlights")
public class StreetlightController {

    private static final Logger log = LoggerFactory.getLogger(StreetlightController.class);
    private final StreetlightService streetlightService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public StreetlightController(StreetlightService streetlightService,
                                 KafkaTemplate<String, Object> kafkaTemplate) {
        this.streetlightService = streetlightService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Streetlights Kafka API");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{streetlightId}/turn-on")
    public ResponseEntity<Map<String, String>> sendTurnOnCommand(@PathVariable String streetlightId) {
        String topic = String.format("smartylighting.streetlights.1.0.action.%s.turn.on", streetlightId);
        TurnOnOffCommand command = new TurnOnOffCommand("on", LocalDateTime.now(), 50);

        log.info("Sending TURN ON command to topic: {}", topic);
        kafkaTemplate.send(topic, streetlightId, command);

        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Turn ON command sent to streetlight %s", streetlightId));
        response.put("topic", topic);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{streetlightId}/turn-off")
    public ResponseEntity<Map<String, String>> sendTurnOffCommand(@PathVariable String streetlightId) {
        String topic = String.format("smartylighting.streetlights.1.0.action.%s.turn.off", streetlightId);
        TurnOnOffCommand command = new TurnOnOffCommand("off", LocalDateTime.now(), 50);

        log.info("Sending TURN OFF command to topic: {}", topic);
        kafkaTemplate.send(topic, streetlightId, command);

        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Turn OFF command sent to streetlight %s", streetlightId));
        response.put("topic", topic);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{streetlightId}/dim")
    public ResponseEntity<Map<String, String>> sendDimCommand(
            @PathVariable String streetlightId,
            @RequestParam Integer percentage) {

        if (percentage < 0 || percentage > 100) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Percentage must be between 0 and 100");
            return ResponseEntity.badRequest().body(error);
        }

        String topic = String.format("smartylighting.streetlights.1.0.action.%s.dim", streetlightId);
        DimLightCommand command = new DimLightCommand(percentage, LocalDateTime.now(), 50);

        log.info("Sending DIM command to topic: {} with percentage: {}", topic, percentage);
        kafkaTemplate.send(topic, streetlightId, command);

        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Dim command sent to streetlight %s - %d%%",
                streetlightId, percentage));
        response.put("topic", topic);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{streetlightId}/measure")
    public ResponseEntity<Map<String, String>> measureLight(@PathVariable String streetlightId) {
        log.info("Triggering manual light measurement for streetlight: {}", streetlightId);
        streetlightService.measureLight(streetlightId);

        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Light measurement triggered for streetlight %s", streetlightId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{streetlightId}/state")
    public ResponseEntity<?> getState(@PathVariable String streetlightId) {
        StreetlightService.StreetlightState state = streetlightService.getState(streetlightId);

        if (state == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", String.format("Streetlight %s not found", streetlightId));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        return ResponseEntity.ok(state);
    }

    @GetMapping("/states")
    public ResponseEntity<Map<String, StreetlightService.StreetlightState>> getAllStates() {
        Map<String, StreetlightService.StreetlightState> states = streetlightService.getAllStates();
        return ResponseEntity.ok(states);
    }
}