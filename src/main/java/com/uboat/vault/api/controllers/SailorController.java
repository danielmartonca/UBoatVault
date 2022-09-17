package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.model.http.new_requests.RequestPulse;
import com.uboat.vault.api.model.http.requests.SelectClientRequest;
import com.uboat.vault.api.model.http.response.JourneyConnectionResponse;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.sailing.Journey;
import com.uboat.vault.api.services.JourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/sailor")
public class SailorController {
    private final JourneyService journeyService;

    @Autowired
    public SailorController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @Operation(summary = "Records the location of the sailor calling it and updates its status to looking for clients if specified in the request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The last known location of the sailor was updated and the status was saved.", content = @Content(mediaType = "application/json")),
    })
    @PostMapping(value = "/pulse", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UBoatResponse> pulse(@RequestHeader(value = "Authorization") String authorizationHeader,
                                               @RequestBody RequestPulse pulse) {
        var uBoatResponse = journeyService.pulse(authorizationHeader, pulse);
        if (uBoatResponse.getHeader() == UBoatStatus.PULSE_SUCCESSFUL)
            return ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
    }

    @PostMapping(value = "/findClients")
    public ResponseEntity<List<Journey>> findClients(@RequestBody Account request) {

        var response = journeyService.findClients(request);
        if (response == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/selectClient")
    public ResponseEntity<JourneyConnectionResponse> selectClient(@RequestBody SelectClientRequest request) {

        var response = journeyService.selectClient(request.getAccount(), request.getJourney());
        if (response == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        var backendResponse = JourneyConnectionResponse.builder()
                .status(response)
                .message(response.getMsg())
                .build();

        return new ResponseEntity<>(backendResponse, HttpStatus.OK);
    }
}
