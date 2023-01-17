package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.sailing.JourneyLocationInfo;
import com.uboat.vault.api.model.domain.sailing.LatLng;
import com.uboat.vault.api.model.domain.sailing.Location;
import lombok.Getter;

import javax.validation.constraints.NotNull;

public class SailDTO {
    @NotNull
    @Getter
    private final LatLng coordinates;

    @Getter
    private final LocationDataDTO locationDataDTO;

    @Getter
    private final String address;

    public SailDTO(JourneyLocationInfo lastKnownLocation) {
        this.coordinates = lastKnownLocation.getLocation().getCoordinates();
        this.address = lastKnownLocation.getLocation().getAddress();
        this.locationDataDTO = lastKnownLocation.getLocationData() == null ? null : new LocationDataDTO(lastKnownLocation.getLocationData());
    }

    public SailDTO(Location location) {
        this.coordinates = location.getCoordinates();
        this.address = location.getAddress();
        this.locationDataDTO = null;
    }
}
