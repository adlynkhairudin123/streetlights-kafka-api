package com.smartylighting.streetlights.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartylighting.streetlights.model.command.DimLightCommand;
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
public class DimLightConsumer {

    private static final Logger log = LoggerFactory.getLogger(DimLightConsumer.class);
    private final StreetlightService streetlightService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DimLightConsumer(StreetlightService streetlightService, ObjectMapper objectMapper) {
        this.streetlightService = streetlightService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topicPattern = "smartylighting\\.streetlights\\.1\\.0\\.action\\..*\\.dim",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDimCommand(
            @Payload Map<String, Object> payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {

        log.info("Received DIM command from topic: {}", topic);
        log.debug("Message key: {}, Payload: {}", key, payload);

        try {
            DimLightCommand command = objectMapper.convertValue(payload, DimLightCommand.class);
            String streetlightId = extractStreetlightId(topic);

            log.debug("Parsed command: {}", command);

            streetlightService.dim(streetlightId, command);

            log.info("Successfully processed DIM command for streetlight: {} to {}%",
                    streetlightId, command.getPercentage());

        } catch (Exception e) {
            log.error("Failed to process DIM command: {}", e.getMessage(), e);
        }
    }

    private String extractStreetlightId(String topic) {
        String[] parts = topic.split("\\.");
        if (parts.length >= 6) {
            return parts[5]; // streetlightId at index 5
        }
        log.warn("Could not extract streetlightId from topic: {}", topic);
        return "unknown";
    }
}