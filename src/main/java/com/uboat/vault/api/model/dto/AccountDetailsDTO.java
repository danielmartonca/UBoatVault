package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.account.Account;
import com.uboat.vault.api.model.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsDTO {
    private UserType type;
    private String fullName;
    private String email;

    public AccountDetailsDTO(Account account) {
        this.type = account.getType();

        this.fullName = account.getAccountDetails().getFullName();
        if (this.fullName == null)
            this.fullName = account.getUsername();

        this.email = account.getAccountDetails().getEmail();
    }
}
