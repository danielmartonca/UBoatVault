package com.uboat.vault.api.presentation.controllers;

import com.uboat.vault.api.business.services.JourneyService;
import com.uboat.vault.api.model.dto.LocationDataDTO;
import com.uboat.vault.api.model.dto.UBoatDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/sailing")
public class JourneyController {
    private final JourneyService journeyService;

    @Operation(summary = "Fetches the current journey if the client/sailor is in a journey or null otherwise.")
    @GetMapping(value = "/journey")
    public ResponseEntity<UBoatDTO> journey(@RequestHeader(value = "Authorization") String authorizationHeader) {
        var responseBody = journeyService.getOngoingJourney(authorizationHeader);

        return switch (responseBody.getHeader()) {
            case ONGOING_JOURNEY_RETRIEVED, ONGOING_JOURNEY_NOT_FOUND ->
                    ResponseEntity.status(HttpStatus.OK).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Called by both the client and the sailor to update their position during a Journey.")
    @PostMapping(value = "/sail")
    public ResponseEntity<UBoatDTO> sail(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody LocationDataDTO locationDataDTO) {
        var responseBody = journeyService.sail(authorizationHeader, locationDataDTO);

        return switch (responseBody.getHeader()) {
            case SAIL_RECORDED -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            case NOT_SAILING -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }
}
