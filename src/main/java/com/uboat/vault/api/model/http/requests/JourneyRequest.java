package com.uboat.vault.api.model.http.requests;

import com.uboat.vault.api.model.other.LatLng;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.sailing.LocationData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class JourneyRequest {
    private Account clientAccount;
    private LocationData currentLocationData;
    private LatLng destinationCoordinates;
}
