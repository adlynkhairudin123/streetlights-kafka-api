package com.smartylighting.streetlights.model.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class TurnOnOffCommand {

    @NotNull(message = "Command cannot be null")
    @Pattern(regexp = "^(on|off)$", message = "Command must be either 'on' or 'off'")
    @JsonProperty("command")
    private String command;

    @NotNull(message = "SentAt timestamp cannot be null")
    @JsonProperty("sentAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime sentAt;

    @JsonProperty("my-app-header")
    private Integer myAppHeader;

    public TurnOnOffCommand() {
    }

    public TurnOnOffCommand(String command, LocalDateTime sentAt, Integer myAppHeader) {
        this.command = command;
        this.sentAt = sentAt;
        this.myAppHeader = myAppHeader;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
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
        return "TurnOnOffCommand{" +
                "command='" + command + '\'' +
                ", sentAt=" + sentAt +
                ", myAppHeader=" + myAppHeader +
                '}';
    }
}