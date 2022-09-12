package com.uboat.vault.api.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UBoatStatus {
    VAULT_INTERNAL_SERVER_ERROR(-1, "UBoat vault has encountered an internal exception. Please report to administration.", "An unexpected issue occurred. Please try again"),
    // /api/checkUsername
    USERNAME_ACCEPTED(1, "Username not found in database", "Username is not used."),
    USERNAME_ALREADY_USED(2, "Username found in database", "Username is already used."),

    // /api/checkPhoneNumber
    PHONE_NUMBER_ACCEPTED(1, "Phone number was not found in database", "Phone number is not used."),
    PHONE_NUMBER_ALREADY_USED(1, "Phone number found in database", "Phone number is already used."),

    // /api/checkDeviceRegistration
    DEVICE_NOT_REGISTERED(1, "Device unique identifier and sim cards not found in the database.", "Current device is not used."),
    DEVICE_INFO_ALREADY_USED(2, "Device info unique identifier code already present in the database.", "Your phone is already used by another account."),
    SIM_ALREADY_USED(3, "Sim card already found in the database.", "Your sim card is already used."),

    // /api/verifyJwt

    JWT_VALID(1, "The jwt token extracted from the header of the request is valid.", "You are authenticated"),
    JWT_INVALID(0, "The jwt token extracted from the header of the request is not valid.", "You are not authenticated"),
    MISSING_BEARER(2, "The request contains 'Authorization' header but is missing the 'Bearer' value.", "An unexpected issue occurred. Please try again"),
    INVALID_BEARER_FORMAT(3, "The request contains the 'Bearer' header but the format is invalid.", "An unexpected issue occurred. Please try again"),

    // /api/requestRegistration
    ACCOUNT_REQUESTED_REGISTRATION_ACCEPTED(1, "A new registration token was created.", "Registration requested successfully"),
    ACCOUNT_ALREADY_EXISTS_BY_CREDENTIALS(2, "The account cannot be registered because the credentials already exist in the database.", "Credentials already used"),
    ACCOUNT_ALREADY_PENDING_REGISTRATION(0, "The given credentials have already requested a registration. Pending registration token returned.", "Already requested registration"),

    // /api/register
    MISSING_RTOKEN(2, "The request contains 'Authorization' header but is missing the 'RToken' value.", "An unexpected issue occurred. Please try again"),
    INVALID_RTOKEN_FORMAT(3, "The request contains the 'RToken' header but the format is invalid.", "An unexpected issue occurred. Please try again"),
    RTOKEN_NOT_FOUND_IN_DATABASE(4, "The RToken values extracted from the authorization header does not exist in the database", "An unexpected issue occurred. Please try again"),
    RTOKEN_AND_ACCOUNT_NOT_MATCHING(5, "The RToken values extracted from the authorization header does not match with the account in the request", "An unexpected issue occurred. Please try again"),
    MISSING_REGISTRATION_DATA_OR_PHONE_NUMBER(6, "The account given in the request is missing registration data or phone number information", "An unexpected issue occurred. Please try again"),
    REGISTRATION_SUCCESSFUL(1, "Registration for the account was successful. JWT is present in the response body.", "Registration successful");

    private final int code;
    private final String serverMessage;
    private final String clientMessage;
}
