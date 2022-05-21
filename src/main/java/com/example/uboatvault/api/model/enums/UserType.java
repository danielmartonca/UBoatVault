package com.example.uboatvault.api.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public enum UserType {
    SAILOR("SAILOR"), CLIENT("CLIENT");

    @Getter
    private final String type;

    UserType(String type) {
        this.type = type;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserType fromType(@JsonProperty("type") String type) {
        for (UserType r : UserType.values()) {
            if (r.getType().equals(type)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Invalid UserType '" + type + "' inserted.");
    }

    @Override
    public String toString() {
        return type;
    }
}
