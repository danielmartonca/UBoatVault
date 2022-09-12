package com.uboat.vault.api.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UBoatStatus {
    // /api/checkUsername
    USERNAME_ACCEPTED(1, "Username not found in database", "Username is not used."),
    USERNAME_ALREADY_USED(2, "Username found in database", "Username is already used."),

    // /api/checkPhoneNumber
    PHONE_NUMBER_ACCEPTED(1, "Phone number was not found in database", "Phone number is not used."),
    PHONE_NUMBER_ALREADY_USED(1, "Phone number found in database", "Phone number is already used."),

    // /api/checkDeviceRegistration
    DEVICE_NOT_REGISTERED(1, "Device unique identifier and sim cards not found in the database.", "Current device is not used."),
    DEVICE_INFO_ALREADY_USED(2, "Device info unique identifier code already present in the database.", "Your phone is already used by another account."),
    SIM_ALREADY_USED(3, "Sim card already found in the database.", "Your sim card is already used.");


    private final int code;
    private final String serverMessage;
    private final String clientMessage;
}
