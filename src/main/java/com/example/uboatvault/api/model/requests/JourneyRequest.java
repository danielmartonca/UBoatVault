package com.example.uboatvault.api.model.requests;

import com.example.uboatvault.api.model.other.LatLng;
import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.location.LocationData;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class JourneyRequest {
    private Account clientAccount;
    private LocationData currentLocationData;
    private LatLng destinationCoordinates;
}
