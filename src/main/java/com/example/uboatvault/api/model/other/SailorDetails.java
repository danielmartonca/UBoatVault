package com.example.uboatvault.api.model.other;

import com.example.uboatvault.api.model.enums.Currency;
import com.example.uboatvault.api.model.persistence.location.LocationData;
import lombok.*;

import java.sql.Timestamp;
import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SailorDetails {
    private String sailorId;
    private String sailorName;
    private LocationData locationData;
    private Date estimatedTimeOfArrival;
    private Timestamp estimatedDuration;
    private String estimatedCost;
    private Currency estimatedCostCurrency;
    private double averageRating;
}