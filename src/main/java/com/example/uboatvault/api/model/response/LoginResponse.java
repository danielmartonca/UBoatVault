package com.example.uboatvault.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponse {
    private Boolean hasSucceeded;
    private String token;
}
