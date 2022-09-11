package com.uboat.vault.api.model.requests;

import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.account.info.CreditCard;
import lombok.Getter;
import lombok.Setter;

public class CreditCardRequest {
    @Getter
    @Setter
    private Account account;
    @Getter
    @Setter
    private CreditCard card;
}
