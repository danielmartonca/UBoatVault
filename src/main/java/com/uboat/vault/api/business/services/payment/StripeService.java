package com.uboat.vault.api.business.services.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.uboat.vault.api.model.domain.sailing.Payment;
import com.uboat.vault.api.model.enums.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {
    @Value("${uboat.stripe_public_key}")
    private String stripePublicKey;
    @Value("${uboat.stripe_private_key}")
    private String stripePrivateKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripePrivateKey;
    }


    public void pay(Payment payment) throws StripeException {
        if (payment.getPaymentType() == PaymentType.CASH)
            throw new RuntimeException("Stripe charge attempted for a cash payment.");

        Map<String, Object> params = new HashMap<>();
        params.put("amount", (int) (payment.getAmount() * 100));
        params.put("currency", payment.getCurrency().getCurrency().toLowerCase()); //Three-letter ISO currency code, in lowercase. Must be a supported currency.
        params.put("source", payment.getCreditCard().getNumber());

        Charge charge = Charge.create(params);
    }
}
