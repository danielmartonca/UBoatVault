package com.example.uboatvault.api.model.response;

import com.example.uboatvault.api.model.persistence.account.info.CreditCard;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public class CreditCardResponse {
    @Getter
    @Setter
    private Set<CreditCard> cards;

    public CreditCardResponse(Set<CreditCard> creditCards) {
        this.cards=new HashSet<>();
        this.cards.addAll(creditCards);
    }
}
