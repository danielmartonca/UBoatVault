package com.uboat.vault.api.presentation.controllers;

import com.uboat.vault.api.business.services.JourneyService;
import com.uboat.vault.api.business.services.payment.PaymentService;
import com.uboat.vault.api.model.dto.LocationDataDTO;
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
@RequiredArgsConstructor
@RequestMapping("api/sailing")
public class JourneyController {
    private final JourneyService journeyService;
    private final PaymentService paymentService;

    @Operation(summary = "Retrieves the last {ridesRequested} number of journeys for the client extracted from the JWT. ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The journeys have been retrieved successfully", content = @Content(mediaType = "application/json")),})
    @GetMapping(value = "/journeys")
    public ResponseEntity<UBoatDTO> journeys(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam @Min(1) @Max(3) Integer ridesRequested) {
        var uBoatResponse = journeyService.getMostRecentJourneys(authorizationHeader, ridesRequested);

        if (uBoatResponse.getHeader() == UBoatStatus.MOST_RECENT_JOURNEYS_RETRIEVED)
            return ResponseEntity.status(HttpStatus.OK).body(uBoatResponse);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uBoatResponse);
    }

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
            case SAIL_RECORDED, NOT_SAILING, LOST_CONNECTION -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Called by both the client and the sailor with different outcomes. " +
            "If the client calls it, it will engage an automatic card payment on the ongoing journey or get the status of the payment. " +
            "If the sailor calls it, its use case is to confirm that the client has completed the payment in cash.")
    @PostMapping(value = "/cardPay")
    public ResponseEntity<UBoatDTO> cardPay(@RequestHeader(value = "Authorization") String authorizationHeader) {

        var responseBody = paymentService.cardPay(authorizationHeader);

        return switch (responseBody.getHeader()) {
            case NO_JOURNEY_TO_PAY, PAYMENT_COMPLETED, PAYMENT_NOT_COMPLETED, CARD_PAYMENT_NOT_SUCCESSFUL, INVALID_PAYMENT_METHOD ->
                    ResponseEntity.status(HttpStatus.OK).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }

    @Operation(summary = "Called by both the client and the sailor with different outcomes. " +
            "If the client calls it, it will engage an automatic card payment on the ongoing journey or get the status of the payment. " +
            "If the sailor calls it, its use case is to confirm that the client has completed the payment in cash.")
    @PostMapping(value = "/cashPay")
    public ResponseEntity<UBoatDTO> cashPay(@RequestHeader(value = "Authorization") String authorizationHeader) {

        var responseBody = paymentService.cashPay(authorizationHeader);

        return switch (responseBody.getHeader()) {
            case PAYMENT_COMPLETED, PAYMENT_NOT_COMPLETED, NO_JOURNEY_TO_PAY ->
                    ResponseEntity.status(HttpStatus.OK).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }
}
