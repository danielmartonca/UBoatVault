package com.uboat.vault.api.model.dto;

import com.uboat.vault.api.model.domain.sailing.Payment;
import com.uboat.vault.api.model.enums.Currency;
import com.uboat.vault.api.model.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private PaymentType paymentType;
    private Currency currency;
    private double amount;

    public PaymentDTO(Payment payment) {
        this.paymentType = payment.getPaymentType();
        this.currency = payment.getCurrency();
        this.amount = payment.getAmount();
    }
}
