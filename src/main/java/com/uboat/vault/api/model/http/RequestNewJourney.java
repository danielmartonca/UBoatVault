package com.uboat.vault.api.model.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uboat.vault.api.model.other.LatLng;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestNewJourney {
    private LatLng currentCoordinates;
    private LatLng destinationCoordinates;

    //only used for /chooseJourney
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String currentAddress;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String destinationAddress;
}