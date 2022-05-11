package com.example.uboatvault.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RegistrationDataResponse {
    private final Boolean isDeviceRegistered;
    private final String tokenValue;

    public RegistrationDataResponse(Boolean isDeviceRegistered) {
        this.isDeviceRegistered = isDeviceRegistered;
        this.tokenValue = null;
    }
}
