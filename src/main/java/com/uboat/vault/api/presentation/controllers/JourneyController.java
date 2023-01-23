package com.uboat.vault.api.presentation.controllers;

import com.uboat.vault.api.business.services.JourneyService;
import com.uboat.vault.api.business.services.payment.PaymentService;
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
    private final PaymentService paymentService;

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
    @PostMapping(value = "/pay")
    public ResponseEntity<UBoatDTO> pay(@RequestHeader(value = "Authorization") String authorizationHeader) {

        var responseBody = paymentService.pay(authorizationHeader);

        return switch (responseBody.getHeader()) {
            case PAYMENT_COMPLETED, PAYMENT_NOT_COMPLETED,NO_JOURNEY_TO_PAY -> ResponseEntity.status(HttpStatus.OK).body(responseBody);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        };
    }
}
