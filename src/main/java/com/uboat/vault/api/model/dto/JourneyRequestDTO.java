package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.sailing.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JourneyRequestDTO {
    @NotNull
    private Location clientLocation;
    @NotNull
    private Location pickupLocation;
    @NotNull
    private Location destinationLocation;
}
