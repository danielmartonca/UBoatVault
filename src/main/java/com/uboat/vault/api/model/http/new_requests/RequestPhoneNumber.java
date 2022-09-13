package com.uboat.vault.api.model.http.new_requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPhoneNumber {
    @NotNull
    private String phoneNumber;
    @NotNull
    private String dialCode;
    @NotNull
    private String isoCode;
}
