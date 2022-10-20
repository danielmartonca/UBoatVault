package com.uboat.vault.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PulseDTO {
    private LocationDataDTO locationData;
    boolean lookingForClients;
}
