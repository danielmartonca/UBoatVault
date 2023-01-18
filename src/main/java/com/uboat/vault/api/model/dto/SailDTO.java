package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.sailing.Journey;
import com.uboat.vault.api.model.domain.sailing.JourneyLocationInfo;
import com.uboat.vault.api.model.domain.sailing.LatLng;
import com.uboat.vault.api.model.domain.sailing.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SailDTO {
    private String state;

    private LatLng coordinates;

    private LocationDataDTO locationDataDTO;


    private String address;


    public SailDTO(Journey journey, JourneyLocationInfo lastKnownLocation) {
        this.state = journey.getState().name();
        this.coordinates = lastKnownLocation.getLocation().getCoordinates();
        this.address = lastKnownLocation.getLocation().getAddress();
        this.locationDataDTO = lastKnownLocation.getLocationData() == null ? null : new LocationDataDTO(lastKnownLocation.getLocationData());
    }

    public SailDTO(Journey journey, Location location) {
        this.state = journey.getState().name();
        this.coordinates = location.getCoordinates();
        this.address = location.getAddress();
        this.locationDataDTO = null;
    }
}
