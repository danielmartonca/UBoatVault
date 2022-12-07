package com.uboat.vault.api.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum PaymentType {
    CASH, CARD;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PaymentType fromPaymentType(@JsonProperty("paymentType") String paymentType) {
        for (var r : PaymentType.values()) {
            if (r.name().equals(paymentType)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Invalid PaymentType '" + paymentType + "' inserted.");
    }
}
