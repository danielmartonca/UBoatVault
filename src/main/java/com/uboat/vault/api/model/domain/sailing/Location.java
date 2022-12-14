package com.uboat.vault.api.model.domain.sailing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Location {
    @NotNull
    @Getter
    @Setter
    private LatLng coordinates;

    @Getter
    @Setter
    private String address;

    public Location(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public Location(LocationData locationData) {
        this(new LatLng(locationData));
    }
}
