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
public class RequestCreditCard {
    @NotNull
    private String number;
    @NotNull
    private String ownerFullName;
    @NotNull
    private String cvc;
    @NotNull
    private String expirationDate;
}
