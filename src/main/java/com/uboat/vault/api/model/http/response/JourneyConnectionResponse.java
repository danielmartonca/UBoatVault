package com.uboat.vault.api.model.http.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class JourneyConnectionResponse {
    String message;
    PossibleResponse status;

    @AllArgsConstructor
    @Getter
    public enum PossibleResponse {
        ERROR(null),
        SERVER_ERROR("Internal server error."),
        SAILOR_NOT_FOUND("Did not find the active sailor."),
        JOURNEY_NOT_FOUND("Did not find journey given."),
        CONNECT_TO_SAILOR_SUCCESS("Driver was notified of journey request."),
        SELECT_CLIENT_SUCCESS("Client was notified that he is selected.");
        final String msg;
    }
}
