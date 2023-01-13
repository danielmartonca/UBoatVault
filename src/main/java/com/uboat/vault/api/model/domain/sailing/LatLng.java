package com.uboat.vault.api.model.domain.sailing;

import com.uboat.vault.api.model.dto.LocationDataDTO;
import lombok.*;

import javax.persistence.Embeddable;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class LatLng {
    private double latitude;
    private double longitude;

    public LatLng(LocationData locationData) {
        this.latitude = Double.parseDouble(locationData.getLatitude());
        this.longitude = Double.parseDouble(locationData.getLongitude());
    }

    public LatLng(LocationDataDTO locationData) {
        this.latitude = Double.parseDouble(locationData.getLatitude());
        this.longitude = Double.parseDouble(locationData.getLongitude());
    }
}