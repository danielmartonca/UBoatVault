package com.uboat.vault.api.model.http;

import com.uboat.vault.api.model.persistence.account.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestMissingAccountInformation {
    @NotNull
    private String username;
    @NotNull
    private RequestPhoneNumber phoneNumber;

    public RequestMissingAccountInformation(Account account) {
        this.username = account.getUsername();
        var accountPhoneNumber = account.getPhoneNumber();
        this.phoneNumber = new RequestPhoneNumber(accountPhoneNumber.getPhoneNumber(), accountPhoneNumber.getDialCode(), accountPhoneNumber.getIsoCode());
    }
}
