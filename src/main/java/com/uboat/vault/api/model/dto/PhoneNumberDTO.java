package com.uboat.vault.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberDTO {
    @NotNull
    private String phoneNumber;
    @NotNull
    private String dialCode;
    @NotNull
    private String isoCode;
}
