package com.uboat.vault.api.business.services.payment;

import com.stripe.Stripe;
import com.uboat.vault.api.model.domain.sailing.Payment;
import com.uboat.vault.api.model.enums.CardPaymentStatus;
import com.uboat.vault.api.model.enums.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

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

    public CardPaymentStatus pay(Payment payment) {
        try {
            if (payment.getPaymentType() == PaymentType.CASH)
                return CardPaymentStatus.NOT_A_CARD_PAYMENT;

//            var params = PaymentIntentCreateParams
//                    .builder()
//                    .setAmount((long) (payment.getAmount() * 100))
//                    .setCurrency(payment.getCurrency().getCurrency().toLowerCase())
//                    .addPaymentMethodType("card")
//                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
//                    .setConfirm(true)
//                    .build();
//            var paymentIntent = PaymentIntent.create(params);
//            paymentIntent = paymentIntent.confirm();

            if (payment.getCreditCard().getNumber().contains("4242")) return CardPaymentStatus.INSUFFICIENT_FOUNDS;
            if (payment.getCreditCard().getNumber().contains("1111")) return CardPaymentStatus.DENIED;

            return CardPaymentStatus.SUCCESSFUL;
        } catch (Exception e) {
            log.error("Exception occurred during stripe service's pay.", e);
            return CardPaymentStatus.ERROR;
        }
    }
}
