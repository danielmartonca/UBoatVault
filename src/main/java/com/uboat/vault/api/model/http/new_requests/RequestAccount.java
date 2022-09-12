package com.uboat.vault.api.model.http.new_requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uboat.vault.api.model.persistence.account.info.AccountDetails;
import com.uboat.vault.api.model.persistence.account.info.CreditCard;
import com.uboat.vault.api.model.persistence.account.info.PhoneNumber;
import com.uboat.vault.api.model.persistence.account.info.RegistrationData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestAccount {
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

}
