package com.uboat.vault.api.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UBoatStatus {
    USERNAME_ACCEPTED(1, "Username not found in database", "Username is not used."),
    USERNAME_ALREADY_USED(2, "Username found in database", "Username is already used."),

    PHONE_NUMBER_ACCEPTED(1, "Phone number was not found in database", "Phone number is not used."),

    PHONE_NUMBER_ALREADY_USED(1, "Phone number found in database", "Phone number is already used.");

    private final int code;
    private final String serverMessage;
    private final String clientMessage;
}
