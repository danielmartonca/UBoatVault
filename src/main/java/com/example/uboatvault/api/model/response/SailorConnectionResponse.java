package com.example.uboatvault.api.model.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SailorConnectionResponse {
    String message;

    @AllArgsConstructor
    @Getter
    public enum PossibleResponse {
        CLIENT_ERROR(null),
        SERVER_ERROR("Internal server error."),
        SAILOR_NOT_FOUND("Did not find the active sailor."),
        SUCCESS("Driver was notified of journey request.");

        final String msg;
    }
}
