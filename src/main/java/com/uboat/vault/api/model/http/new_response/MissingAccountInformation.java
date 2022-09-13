package com.uboat.vault.api.model.http.new_response;

import com.uboat.vault.api.model.http.new_requests.RequestPhoneNumber;
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
public class MissingAccountInformation {
    @NotNull
    private String username;
    @NotNull
    private RequestPhoneNumber phoneNumber;

    public MissingAccountInformation(Account account) {
        this.username = account.getUsername();
        var accountPhoneNumber = account.getPhoneNumber();
        this.phoneNumber = new RequestPhoneNumber(accountPhoneNumber.getPhoneNumber(), accountPhoneNumber.getDialCode(), accountPhoneNumber.getIsoCode());
    }
}
