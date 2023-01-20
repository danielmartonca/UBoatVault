package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.sailing.Payment;
import com.uboat.vault.api.model.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private String paymentType;

    private String cardNumber;
    private Currency currency;
    private double amount;

    public PaymentDTO(Payment payment) {
        if (payment.getPaymentType() == null) this.paymentType = null;
        else
            this.paymentType = payment.getPaymentType().name().toUpperCase();
        this.currency = payment.getCurrency();
        this.amount = payment.getAmount();
    }
}
