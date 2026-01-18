package com.smartylighting.streetlights.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartylighting.streetlights.model.command.TurnOnOffCommand;
import com.smartylighting.streetlights.service.StreetlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TurnOnOffConsumer {

    private static final Logger log = LoggerFactory.getLogger(TurnOnOffConsumer.class);

    private final StreetlightService streetlightService;
    private final ObjectMapper objectMapper;

    @Autowired
    public TurnOnOffConsumer(StreetlightService streetlightService, ObjectMapper objectMapper) {
        this.streetlightService = streetlightService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topicPattern = "smartylighting\\.streetlights\\.1\\.0\\.action\\..*\\.turn\\.on",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTurnOnCommand(
            @Payload Map<String, Object> payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {

        log.info("Received TURN ON command from topic: {}", topic);
        log.debug("Message key: {}, Payload: {}", key, payload);

        try {
            TurnOnOffCommand command = objectMapper.convertValue(payload, TurnOnOffCommand.class);
            String streetlightId = extractStreetlightId(topic);

            log.debug("Parsed command: {}", command);

            // Process the command
            streetlightService.turnOn(streetlightId, command);

            log.info("Successfully processed TURN ON command for streetlight: {}", streetlightId);

        } catch (Exception e) {
            log.error("Failed to process TURN ON command: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topicPattern = "smartylighting\\.streetlights\\.1\\.0\\.action\\..*\\.turn\\.off",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTurnOffCommand(
            @Payload Map<String, Object> payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {

        log.info("Received TURN OFF command from topic: {}", topic);
        log.debug("Message key: {}, Payload: {}", key, payload);

        try {
            TurnOnOffCommand command = objectMapper.convertValue(payload, TurnOnOffCommand.class);
            String streetlightId = extractStreetlightId(topic);

            log.debug("Parsed command: {}", command);

            streetlightService.turnOff(streetlightId, command);

            log.info("Successfully processed TURN OFF command for streetlight: {}", streetlightId);

        } catch (Exception e) {
            log.error("Failed to process TURN OFF command: {}", e.getMessage(), e);
        }
    }

    private String extractStreetlightId(String topic) {
        String[] parts = topic.split("\\.");
        if (parts.length >= 6) {
            return parts[5]; // streetlightId is at index 5
        }
        log.warn("Could not extract streetlightId from topic: {}", topic);
        return "unknown";
    }
}