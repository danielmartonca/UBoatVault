package com.uboat.vault.api.model.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestPulse {
    private RequestLocationData locationData;
    boolean lookingForClients;
}
