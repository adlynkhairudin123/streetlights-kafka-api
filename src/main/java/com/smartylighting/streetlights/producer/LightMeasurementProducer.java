package com.smartylighting.streetlights.producer;

import com.smartylighting.streetlights.model.event.LightMeasuredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class LightMeasurementProducer {

    private static final Logger log = LoggerFactory.getLogger(LightMeasurementProducer.class);
    private static final String TOPIC_TEMPLATE = "smartylighting.streetlights.1.0.event.%s.lighting.measured";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public LightMeasurementProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishLightMeasurement(String streetlightId, LightMeasuredEvent event) {
        String topic = String.format(TOPIC_TEMPLATE, streetlightId);

        log.info("Publishing light measurement for streetlight {} to topic {}", streetlightId, topic);
        log.debug("Event payload: {}", event);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, streetlightId, event);

        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Successfully published light measurement for streetlight {} - Offset: {}, Partition: {}",
                        streetlightId,
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
            } else {
                log.error("Failed to publish light measurement for streetlight {}: {}",
                        streetlightId, exception.getMessage(), exception);
            }
        });
    }

    public SendResult<String, Object> publishLightMeasurementSync(String streetlightId, LightMeasuredEvent event)
            throws Exception {
        String topic = String.format(TOPIC_TEMPLATE, streetlightId);

        log.info("Publishing light measurement (sync) for streetlight {} to topic {}", streetlightId, topic);
        log.debug("Event payload: {}", event);

        SendResult<String, Object> result = kafkaTemplate.send(topic, streetlightId, event).get();

        log.info("Successfully published light measurement for streetlight {} - Offset: {}, Partition: {}",
                streetlightId,
                result.getRecordMetadata().offset(),
                result.getRecordMetadata().partition());

        return result;
    }
}