package com.example.uboatvault.api.model.requests;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.persistence.CreditCard;
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
