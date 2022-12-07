package com.uboat.vault.api.utilities;

import com.uboat.vault.api.model.dto.UBoatDTO;
import com.uboat.vault.api.model.enums.UBoatStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HeadersUtils {
    public static ResponseEntity<UBoatDTO> parseAuthorizationHeaderForRToken(String authorizationHeader) {
        if (!authorizationHeader.contains("RToken "))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UBoatDTO(UBoatStatus.MISSING_RTOKEN, false));

        var split = authorizationHeader.split(" ");
        if (split.length != 2)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UBoatDTO(UBoatStatus.INVALID_RTOKEN_FORMAT, false));

        var registrationToken = split[1];

        if (registrationToken.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UBoatDTO(UBoatStatus.INVALID_RTOKEN_FORMAT, false));

        return null;
    }

    public static String extractSecret(String authorizationHeader) {
        return authorizationHeader.split(" ")[1];
    }
}
