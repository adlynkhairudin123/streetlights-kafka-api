package com.smartylighting.streetlights.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class LightMeasuredEvent {

    @NotNull(message = "Lumens cannot be null")
    @Min(value = 0, message = "Lumens must be non-negative")
    @JsonProperty("lumens")
    private Integer lumens;

    @NotNull(message = "SentAt timestamp cannot be null")
    @JsonProperty("sentAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime sentAt;

    @JsonProperty("my-app-header")
    private Integer myAppHeader;

    public LightMeasuredEvent() {
    }

    public LightMeasuredEvent(Integer lumens, LocalDateTime sentAt, Integer myAppHeader) {
        this.lumens = lumens;
        this.sentAt = sentAt;
        this.myAppHeader = myAppHeader;
    }

    public Integer getLumens() {
        return lumens;
    }

    public void setLumens(Integer lumens) {
        this.lumens = lumens;
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
        return "LightMeasuredEvent{" +
                "lumens=" + lumens +
                ", sentAt=" + sentAt +
                ", myAppHeader=" + myAppHeader +
                '}';
    }
}