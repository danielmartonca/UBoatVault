package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.account.pending.PendingAccount;
import com.uboat.vault.api.model.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    @NotNull
    private UserType type;

    private String username;
    @NotNull
    private String password;

    private PhoneNumberDTO phoneNumber;

    private RegistrationDataDTO registrationData;

    public boolean equalsPendingAccount(PendingAccount pendingAccount) {
        return this.username.equals(pendingAccount.getUsername()) &&
                this.password.equals(pendingAccount.getPassword());
    }
}
