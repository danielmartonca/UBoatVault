package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.account.account.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissingAccountInformationDTO {
    @NotNull
    private String username;
    @NotNull
    private PhoneNumberDTO phoneNumber;

    public MissingAccountInformationDTO(Account account) {
        this.username = account.getUsername();
        var accountPhoneNumber = account.getPhone();
        this.phoneNumber = new PhoneNumberDTO(accountPhoneNumber.getNumber(), accountPhoneNumber.getDialCode(), accountPhoneNumber.getIsoCode());
    }
}
