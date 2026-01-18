package com.smartylighting.streetlights;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application
 *
 * Streetlights Kafka API - AsyncAPI Implementation
 *
 * Goal: To implement an event-driven architecture using Apache Kafka
 * to manage city streetlights remotely.
 *
 * Features:
 * - Turn streetlights ON/OFF via Kafka commands
 * - Dim streetlights to specific brightness levels
 * - Publish real-time light measurement events
 * - SASL/SCRAM authentication support
 *
 * @author Adlyn Akram
 * @version 1.0.0
 */
@SpringBootApplication
public class StreetLightsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreetLightsApplication.class, args);
    }
}