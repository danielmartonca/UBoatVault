package com.uboat.vault.api.model.domain.sailing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Location {
    @Getter
    @Setter
    private LatLng coordinates;

    @Getter
    @Setter
    private String address;
}
