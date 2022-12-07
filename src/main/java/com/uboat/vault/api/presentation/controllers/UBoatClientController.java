package com.uboat.vault.api.presentation.controllers;

import com.uboat.vault.api.business.services.AccountsService;
import com.uboat.vault.api.business.services.JourneyService;
import com.uboat.vault.api.model.dto.JourneyDTO;
import com.uboat.vault.api.model.dto.JourneyRequestDTO;
import com.uboat.vault.api.model.dto.UBoatDTO;
import com.uboat.vault.api.model.enums.UBoatStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("api/client")
@RequiredArgsConstructor
public class UBoatClientController {
    private final JourneyService journeyService;
    private final AccountsService accountsService;

    @Operation(summary = "Retrieves the last {ridesRequested} number of journeys for the client extracted from the JWT. ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The journeys have been retrieved successfully", content = @Content(mediaType = "application/json")),})
    @GetMapping(value = "/mostRecentRides")
    public ResponseEntity<UBoatDTO> mostRecentRides(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam @Min(1) @Max(3) Integer ridesRequested) {
        var uBoatResponse = journeyService.getMostRecentRides(authorizationHeader, ridesRequested);

        if (uBoatResponse.getHeader() == UBoatStatus.MOST_RECENT_RIDES_RETRIEVED)
            return ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
    }

    @Operation(summary = "Retrieves information about the boat of the sailor searched by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The boat details have been retrieved.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "The sailor could not be found by that id.", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/sailorBoat")
    public ResponseEntity<UBoatDTO> getSailorBoat(@RequestParam(name = "sailorId") String sailorId) {
        var uBoatResponse = accountsService.getSailorBoat(sailorId);

        return switch (uBoatResponse.getHeader()) {
            case SAILOR_BOAT_RETRIEVED -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            case SAILOR_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @Operation(summary = "This API retrieves information about possible journeys. " +
            "It fetches all active sailors and calls all services to calculate details about the journey such as: " +
            "sailor id, sailor name, estimated cost, duration, quality etc. " +
            "This API just fetches and calculates data for each possible free sailor. Connection to the sailor can only be established by calling /chooseJourney API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Details about each journey are returned or empty list if no free sailors could be detected.", content = @Content(mediaType = "application/json")),
    })
    @PostMapping(value = "/requestJourney")
    public ResponseEntity<UBoatDTO> requestJourney(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody JourneyRequestDTO request) {
        var uBoatResponse = journeyService.requestJourney(authorizationHeader, request);

        return switch (uBoatResponse.getHeader()) {
            case JOURNEYS_INITIATED, NO_FREE_SAILORS_FOUND -> ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }

    @Operation(summary = "After fetching data with /requestJourney, this API uses that data to establish a possible new Journey request with the sailor: " +
            "A new Journey is created with a status indicating that the client has chosen this Journey. The sailor must confirm this action too by calling: " +
            "/findClients (to fetch all available Journeys selected by the clients) and " +
            "/selectClient (to select the new Journey/or he might choose another one in which case the other journeys with that specific status will be canceled). " +
            "This there is already an ongoing journey request for the client, the old journey request will be canceled.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A new journey was created and the other ones with status CLIENT_ACCEPTED(due to app restart/error etc) are canceled.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Could not find the sailor by sailor ID.", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/chooseJourney")
    public ResponseEntity<UBoatDTO> chooseJourney(@RequestHeader(value = "Authorization") String authorizationHeader,
                                                  @RequestBody JourneyDTO request) {
        var uBoatResponse = journeyService.chooseJourney(authorizationHeader, request);

        return switch (uBoatResponse.getHeader()) {
            case CLIENT_ACCEPTED_JOURNEY -> ResponseEntity.status(HttpStatus.CREATED).body(uBoatResponse);
            case SAILOR_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(uBoatResponse);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        };
    }
}
