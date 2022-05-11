package com.example.uboatvault.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenResponse {
    private Boolean isAccountRegistered;
    private String token;
}
