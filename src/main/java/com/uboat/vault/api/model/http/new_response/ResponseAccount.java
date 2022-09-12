package com.uboat.vault.api.model.http.new_response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.account.info.AccountDetails;
import com.uboat.vault.api.model.persistence.account.info.CreditCard;
import com.uboat.vault.api.model.persistence.account.info.PhoneNumber;
import com.uboat.vault.api.model.persistence.account.info.RegistrationData;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class ResponseAccount {
    @NotNull
    private String username;

    @NotNull
    private String password;

    @NotNull
    private PhoneNumber phoneNumber;

    @NotNull
    private RegistrationData registrationData;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private AccountDetails accountDetails;

    @JsonIgnore
    private Set<CreditCard> creditCards;

    public ResponseAccount(Account account) {
        this.username = account.getUsername();
        this.password = account.getPassword();
        this.phoneNumber = account.getPhoneNumber();
        this.registrationData = account.getRegistrationData();
        this.accountDetails = account.getAccountDetails();
        this.creditCards = account.getCreditCards();
    }
}
