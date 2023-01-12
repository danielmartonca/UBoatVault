package com.uboat.vault.api.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UBoatStatus {
    ACCOUNT_NOT_FOUND(-2, "The credentials in the request don't match to any account from the database.", CommonStatus.UNEXPECTED_ERROR),
    INVALID_AUTHORIZATION_HEADER(-3, "Couldn't complete action because the authorization header format is invalid", CommonStatus.UNEXPECTED_ERROR),
    CREDENTIALS_NOT_MATCHING_JWT(-4, "Couldn't complete action because the credentials of the request are not matching the JWT token.", CommonStatus.UNEXPECTED_ERROR),

    // /api/email
    EMAIL_ACCEPTED(1, "Email not found in database", "Email is not used."),
    EMAIL_ALREADY_USED(2, "Email found in database", "Email already used."),
    EMAIL_INVALID_FORMAT(3, "Email has invalid format.", "Email not acceptable."),

    // /api/username
    USERNAME_ACCEPTED(1, "Username not found in database", "Username is not used."),
    USERNAME_ALREADY_USED(2, "Username found in database", "Username is already used."),
    USERNAME_INVALID_FORMAT(3, "Username has invalid format.", "Username not acceptable."),


    // /api/phoneNumber
    PHONE_NUMBER_ACCEPTED(1, "Phone number was not found in database", "Phone number is not used."),
    PHONE_NUMBER_ALREADY_USED(2, "Phone number found in database", "Phone number is already used."),
    PHONE_NUMBER_INVALID_FORMAT(3, "Phone number has invalid format.", "Phone number not acceptable."),


    // /api/getMissingAccountInformation
    MISSING_ACCOUNT_INFORMATION_RETRIEVED(1, "Retrieved the account in the request missing information.", CommonStatus.SUCCESS),

    // /api/accountDetails
    ACCOUNT_DETAILS_RETRIEVED(1, "Account details were extracted successfully using JWT data.", CommonStatus.SUCCESS),

    // /api/accountDetails
    ACCOUNT_DETAILS_UPDATED(1, "Updated account details with information that was not empty in the request.", "Updated successfully"),

    // /api/creditCards
    CREDIT_CARDS_RETRIEVED(1, "Credit cards were extracted successfully using JWT data.", CommonStatus.SUCCESS),

    // /api/creditCard

    CREDIT_CARD_EXPIRED(2, "Credit card has an expiration date in the past.", "Credit card is expired."),
    CREDIT_CARD_DUPLICATE(0, "Credit card already exists for the account.", "Credit card is already set to your account"),
    CREDIT_CARD_ADDED(1, "Credit card was added to the account.", "Credit card added successfully"),

    // /api/creditCard

    CREDIT_CARD_NOT_FOUND(0, "The credit card is not bounded to the account.", "Credit card is not used"),
    CREDIT_CARD_DELETED(1, "Credit card was deleted from the account.", "Credit card deleted"),

    // /api/boat
    BOAT_RETRIEVED(1, "The boat has been returned by the sailor", CommonStatus.SUCCESS),

    // /api/boat
    BOAT_UPDATED(1, "The boat details have been updated", "Updated your boat details"),

    // /api/journey
    ONGOING_JOURNEY_NOT_FOUND(0, "There is no ongoing journey.", null),
    ONGOING_JOURNEY_RETRIEVED(1, "Ongoing journey was retrieved.", null),

    // /api/sailorDetails
    SAILOR_DETAILS_RETRIEVED(1, "The sailor name has been retrieved", CommonStatus.SUCCESS),
    SAILOR_NOT_FOUND(0, "The sailor couldn't be found by ID", null),
    JOURNEY_FOR_SAILOR_NOT_FOUND(2, "The sailor is valid but no journey could be found for him matching the client.", null),


    // /api/checkDeviceRegistration
    DEVICE_NOT_REGISTERED(1, "Device unique identifier and sim cards not found in the database.", "Current device is not used."),
    DEVICE_INFO_ALREADY_USED(2, "Device info unique identifier code already present in the database.", "Your phone is already used by another account."),
    SIM_ALREADY_USED(3, "Sim card already found in the database.", "Your sim card is already used."),

    // /api/jwt

    JWT_VALID(1, "The jwt token extracted from the header of the request is valid.", "You are authenticated"),
    JWT_INVALID(0, "The jwt token extracted from the header of the request is not valid.", "You are not authenticated"),
    JWT_EXPIRED(-1, "The jwt token extracted from the header of the request is expired.", "You are not authenticated"),
    JWT_ACCOUNT_NOT_FOUND(-2, "The jwt token extracted from the header of the request is valid but no account with the encrypted credentials was found.", "You are not authenticated"),
    MISSING_BEARER(2, "The request contains 'Authorization' header but is missing the 'Bearer' value.", CommonStatus.UNEXPECTED_ERROR),
    INVALID_BEARER_FORMAT(3, "The request contains the 'Bearer' header but the format is invalid.", CommonStatus.UNEXPECTED_ERROR),

    // /api/registrationSms
    REGISTRATION_SMS_SENT(1, "The SMS was sent to the phone number.", "Phone number has been confirmed"),

    // /api/emailVerification
    EMAIL_VERIFIED(1, "The email has been verified by the user.", "Email has been confirmed"),
    EMAIL_NOT_VERIFIED(0, "The email has NOT been verified by the user.", null),

    // /api/requestRegistration
    ACCOUNT_ALREADY_PENDING_REGISTRATION(0, "The given credentials have already requested a registration. Pending registration token returned.", null),
    ACCOUNT_REQUESTED_REGISTRATION_ACCEPTED(1, "A new registration token was created.", null),
    ACCOUNT_ALREADY_EXISTS_BY_CREDENTIALS(2, "The account cannot be registered because the credentials already exist in the database.", "Credentials already used"),


    // /api/register
    MISSING_RTOKEN(2, "The request contains 'Authorization' header but is missing the 'RToken' value.", CommonStatus.UNEXPECTED_ERROR),
    INVALID_RTOKEN_FORMAT(3, "The request contains the 'RToken' header but the format is invalid.", CommonStatus.UNEXPECTED_ERROR),
    RTOKEN_NOT_FOUND_IN_DATABASE(4, "The RToken values extracted from the authorization header does not exist in the database", CommonStatus.UNEXPECTED_ERROR),
    RTOKEN_AND_ACCOUNT_NOT_MATCHING(5, "The RToken values extracted from the authorization header does not match with the account in the request", CommonStatus.UNEXPECTED_ERROR),
    MISSING_REGISTRATION_DATA_OR_PHONE_NUMBER(6, "The account given in the request is missing registration data or phone number information", CommonStatus.UNEXPECTED_ERROR),
    REGISTRATION_SUCCESSFUL(1, "Registration for the account was successful. JWT is present in the response body.", "Registration successful"),


    // /api/login
    CREDENTIALS_NOT_FOUND(2, "The password is not used by any account.", CommonStatus.INVALID_CREDENTIALS),
    INVALID_CREDENTIALS(0, "The username or phone number don't match with the given password", CommonStatus.INVALID_CREDENTIALS),
    LOGIN_SUCCESSFUL(1, "The credentials match. Login was successful", "Logged in successfully"),


    // /images/defaultProfilePicture
    DEFAULT_PROFILE_PICTURE_RETRIEVED(1, "Default profile picture retrieved successfully.", CommonStatus.SUCCESS),


    // /images/defaultProfilePicture
    PROFILE_PICTURE_RETRIEVED(1, "Profile picture has been retrieved.", null),


    // /images/sailorProfilePicture
    SAILOR_PROFILE_PICTURE_RETRIEVED(1, "Sailor profile picture retrieved successfully.", CommonStatus.SUCCESS),
    SAILOR_PROFILE_PICTURE_NOT_SET(0, "Sailor has not set a profile picture.", CommonStatus.SUCCESS),


    // /images/boatImages
    SAILOR_BOAT_IMAGES_RETRIEVED(1, "Sailor boat images retrieved successfully.", CommonStatus.SUCCESS),


    // /images/profileImage

    PROFILE_IMAGE_UPLOADED(1, "Profile image has been uploaded successfully.", "Profile picture updated"),
    PROFILE_IMAGE_ALREADY_EXISTING(0, "Boat image already existed.", "Profile picture updated"),


    // /images/boatImage
    BOAT_IMAGE_UPLOADED(1, "Boat image has been uploaded successfully.", "Boat image uploaded"),
    BOAT_IMAGE_ALREADY_EXISTING(0, "Boat image was already existing for the sailor.", "Image was already uploaded"),

    // /images/boatImagesIdentifiers

    BOAT_IMAGES_HASHES_RETRIEVED(1, "All boat images hash values have been retrieved.", CommonStatus.SUCCESS),

    BOAT_IMAGES_HASHES_EMPTY(0, "Boat does not have any pictures set.", "Boat does not have images"),

    // /images/boatImage

    BOAT_IMAGE_RETRIEVED(1, "Boat image bytes retrieved successfully.", CommonStatus.SUCCESS),
    BOAT_IMAGE_NOT_FOUND(0, "Boat image could not be found by identifier.", "Failed to retrieve image"),

    // /images/boatImage
    BOAT_IMAGE_DELETED(1, "Boat image deleted successfully.", "Boat image deleted"),

    // /api/sailor/pulse
    PULSE_SUCCESSFUL(1, "The pulse has been registered.", null),

    // /api/sailor/findClient
    NO_CLIENT_FOUND(0, "No new client was found.", "No having any client for the moment..."),
    CLIENT_FOUND(1, "Found new client for the sailor", "New client"),

    // /api/sailor/selectClient
    CLIENT_ACCEPTED_JOURNEY(1, "The status of Journey has been updated. The sailor must now accept the client.", null),


    // /api/client/getMostRecentRides
    MOST_RECENT_RIDES_RETRIEVED(1, "Retrieved most recent rides for the client.", null),

    // /api/client/getSailorBoat
    SAILOR_BOAT_RETRIEVED(1, "Sailor boat details have been retrieved successfully.", null),


    // /api/client/requestJourney
    NO_ROUTE_FOUND(0, "Free sailors were found but no possible route could be found.", "No route could be determined. Please advise your location and destination choice or try again in a few moments"),
    JOURNEYS_INITIATED(1, "Free sailors have been found and new journeys have been initiated. ", "Found sailor(s) and route(s)!"),
    NO_FREE_SAILORS_FOUND(2, "No free sailors were found by backend.", "No free sailors were found. Please try again"),

    // /api/client/deleteInitiatedJourneys
    INITIATED_JOURNIES_DELETED(1, "All initiated journies have been deleted.", null),

    // /api/client/journeyStatus
    JOURNEY_WITH_STATE_NOT_FOUND(0, "The journey with the given status was not found.", null),
    JOURNEY_WITH_STATE_FOUND(1, "The journey with the given status was found.", null),

    // /api/client/selectClient
    JOURNEY_NOT_FOUND(2, "The journey could not be found", CommonStatus.UNEXPECTED_ERROR),
    JOURNEY_CONFIRMED(1, "Sailor has selected a journey and canceled all the others.", "Journey confirmed"),
    ANOTHER_JOURNEY_ALREADY_SELECTED(3, "Sailor has already selected another journey. ", "You have already selected another ride. Please wait for the client to cancel it or the journey to expire."),

    VAULT_INTERNAL_SERVER_ERROR(-1, "UBoat vault has encountered an internal exception. Please report to administration.", CommonStatus.UNEXPECTED_ERROR);

    private final int code;
    private final String serverMessage;
    private final String clientMessage;

    private static class CommonStatus {
        public static String UNEXPECTED_ERROR = "An unexpected error occurred. Please try again";
        public static String INVALID_CREDENTIALS = "Invalid credentials. Please try again";
        public static String SUCCESS = "Success";
    }
}
