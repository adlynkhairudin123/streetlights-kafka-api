package com.smartylighting.streetlights;

import com.smartylighting.streetlights.model.command.DimLightCommand;
import com.smartylighting.streetlights.model.command.TurnOnOffCommand;
import com.smartylighting.streetlights.service.StreetlightService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {
        "smartylighting.streetlights.1.0.action.test-001.turn.on",
        "smartylighting.streetlights.1.0.action.test-001.turn.off",
        "smartylighting.streetlights.1.0.action.test-001.dim"
})
public class StreetlightIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private StreetlightService streetlightService;

    @Test
    public void testTurnOnOffDimFlow() throws InterruptedException {

        String id = "test-001";

        // 1) Turn ON
        TurnOnOffCommand onCommand = new TurnOnOffCommand("on", LocalDateTime.now(), 50);
        kafkaTemplate.send("smartylighting.streetlights.1.0.action.test-001.turn.on", id, onCommand);

        Thread.sleep(1000); // wait for consumer to process

        assertTrue(streetlightService.getState(id).isOn());
        assertEquals(100, streetlightService.getState(id).getDimPercentage());

        // 2) Dim 60%
        DimLightCommand dimCommand = new DimLightCommand(60, LocalDateTime.now());
        kafkaTemplate.send("smartylighting.streetlights.1.0.action.test-001.dim", id, dimCommand);

        Thread.sleep(1000);

        assertEquals(60, streetlightService.getState(id).getDimPercentage());

        // 3) Turn OFF
        TurnOnOffCommand offCommand = new TurnOnOffCommand("off", LocalDateTime.now(), 50);
        kafkaTemplate.send("smartylighting.streetlights.1.0.action.test-001.turn.off", id, offCommand);

        Thread.sleep(1000);

        assertFalse(streetlightService.getState(id).isOn());
        assertEquals(60, streetlightService.getState(id).getDimPercentage());
    }
}
