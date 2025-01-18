package com.ka1zerov.nbd.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MessageType {
    MESSAGE("message"),
    USER_CONNECTED("user.connected"),
    USER_DISCONNECTED("user.disconnected");

    @JsonValue
    private final String value;

    public String value() {
        return value;
    }
}
