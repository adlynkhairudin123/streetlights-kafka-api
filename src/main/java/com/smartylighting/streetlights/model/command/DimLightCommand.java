package com.smartylighting.streetlights.model.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class DimLightCommand {

    @NotNull(message = "Percentage cannot be null")
    @Min(value = 0, message = "Percentage must be at least 0")
    @Max(value = 100, message = "Percentage must not exceed 100")
    @JsonProperty("percentage")
    private Integer percentage;

    @NotNull(message = "SentAt timestamp cannot be null")
    @JsonProperty("sentAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime sentAt;

    @JsonProperty("my-app-header")
    private Integer myAppHeader;

    // constructor for Jackson
    public DimLightCommand() {
    }

    public DimLightCommand(Integer percentage, LocalDateTime sentAt) {
        this.percentage = percentage;
        this.sentAt = sentAt;
    }

    public DimLightCommand(Integer percentage, LocalDateTime sentAt, Integer myAppHeader) {
        this.percentage = percentage;
        this.sentAt = sentAt;
        this.myAppHeader = myAppHeader;
    }

    // Getter and Setter
    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Integer getMyAppHeader() {
        return myAppHeader;
    }

    public void setMyAppHeader(Integer myAppHeader) {
        this.myAppHeader = myAppHeader;
    }

    @Override
    public String toString() {
        return "DimLightCommand{" +
                "percentage=" + percentage +
                ", sentAt=" + sentAt +
                ", myAppHeader=" + myAppHeader +
                '}';
    }
}