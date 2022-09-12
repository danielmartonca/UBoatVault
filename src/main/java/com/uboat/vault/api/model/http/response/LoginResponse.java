package com.uboat.vault.api.model.http.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponse {
    private Boolean hasSucceeded;
    private String token;
}
