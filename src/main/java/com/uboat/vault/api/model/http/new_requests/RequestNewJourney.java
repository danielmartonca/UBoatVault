package com.uboat.vault.api.model.http.new_requests;

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
}
