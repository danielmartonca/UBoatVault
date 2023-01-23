package com.uboat.vault.api.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

//Three-letter ISO currency code, in lowercase. Must be a supported currency in lower case format.
public enum Currency {
    EUR("EUR"), RON("RON");

    @Getter
    private final String currency;

    Currency(String currency) {
        this.currency = currency;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Currency fromCurrency(@JsonProperty("currency") String currency) {
        for (Currency r : Currency.values()) {
            if (r.getCurrency().equals(currency)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Invalid Currency '" + currency + "' inserted.");
    }

    @Override
    public String toString() {
        return currency;
    }
}
