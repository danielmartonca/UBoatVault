package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.sailing.LatLng;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JourneyRequestDTO {
    @NotNull
    private LatLng currentCoordinates;
    @NotNull
    private LatLng destinationCoordinates;

    private String currentAddress;
    private String destinationAddress;
}
