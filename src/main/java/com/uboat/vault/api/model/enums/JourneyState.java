package com.uboat.vault.api.model.enums;

public enum JourneyState {
    INITIATED, //when /requestJourney API has created a new Journey but no client/sailor has yet accepted any ride
    SUCCESSFULLY_FINISHED, IN_ERROR,

    CLIENT_ACCEPTED, CLIENT_CANCELED,
    SAILOR_ACCEPTED, SAILOR_CANCELED,

    VERIFYING_PAYMENT,

    SAILING_TO_CLIENT, SAILING_TO_DESTINATION,
}
