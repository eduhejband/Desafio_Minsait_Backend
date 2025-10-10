package com.example.desafio_back.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Type {
    DEPOSIT("DEPOSIT"),
    PAYMENT("PAYMENT");

    private final String value;

    Type(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Type from(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        String normalized = raw.trim().toUpperCase();
        for (Type t : values()) {
            if (t.value.equals(normalized)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Invalid Type: " + raw);
    }
}
