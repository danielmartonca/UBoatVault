package com.uboat.vault.api.model.http.new_requests;

import com.uboat.vault.api.model.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestNewJourneyDetails {
    private String sailorId;
    private String sailorName;
    private RequestLocationData sailorCurrentLocation;
    private Date estimatedTimeOfArrival;
    private Timestamp estimatedDuration;
    private String estimatedCost;
    private Currency estimatedCostCurrency;
    private double averageRating;
    private Double distance;
}
