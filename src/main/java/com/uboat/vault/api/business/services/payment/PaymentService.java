package com.uboat.vault.api.business.services.payment;

import com.uboat.vault.api.business.services.EntityService;
import com.uboat.vault.api.business.services.JwtService;
import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.domain.sailing.Journey;
import com.uboat.vault.api.model.domain.sailing.Payment;
import com.uboat.vault.api.model.dto.UBoatDTO;
import com.uboat.vault.api.model.enums.JourneyState;
import com.uboat.vault.api.model.enums.PaymentType;
import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.persistence.repostiories.JourneyRepository;
import com.uboat.vault.api.persistence.repostiories.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final EntityService entityService;
    private final JwtService jwtService;

    private final StripeService stripeService;

    private final JourneyRepository journeyRepository;
    private final PaymentsRepository paymentsRepository;

    private static final ReentrantLock lock = new ReentrantLock();

    @Transactional
    UBoatDTO cardPayment(Payment payment) {
        lock.lock();
        try {
            if (payment.isCompleted()) {
                log.info("Payment already completed.");
                return new UBoatDTO(UBoatStatus.PAYMENT_COMPLETED, true);
            }

            var paymentStatus = stripeService.pay(payment);
            switch (paymentStatus) {
                case ERROR -> {
                    log.warn("Payment via card not completed due to error");
                    return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
                }   case NOT_A_CARD_PAYMENT -> {
                    log.warn("Stripe payments cannot be completed on a cash payment.");
                    return new UBoatDTO(UBoatStatus.INVALID_PAYMENT_METHOD);
                }
                case DENIED, INSUFFICIENT_FOUNDS -> {
                    log.warn("Payment via card not completed due to {}.", paymentStatus);
                    log.info("Setting new payment method to payment with id {} to cash payment.", payment.getId());
                    payment.setPaymentType(PaymentType.CASH);
                    payment.setCreditCard(null);
                    paymentsRepository.save(payment);
                    return new UBoatDTO(UBoatStatus.CARD_PAYMENT_NOT_SUCCESSFUL, false);
                }
                case SUCCESSFUL -> {
                    log.info("Payment via card completed");
                    payment.complete();
                    payment.getJourney().setState(JourneyState.PAYMENT_VERIFIED);
                    paymentsRepository.save(payment);
                    journeyRepository.save(payment.getJourney());
                    return new UBoatDTO(UBoatStatus.PAYMENT_COMPLETED, true);
                }
                default -> {
                    return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    UBoatDTO cashPayment(Account account, Payment payment) {
        lock.lock();
        try {
            if (payment.isCompleted()) {
                log.info("Payment already completed.");
                return new UBoatDTO(UBoatStatus.PAYMENT_COMPLETED, true);
            }

            if (account.getType() == UserType.CLIENT) {
                log.warn("A cash payment can only be confirmed by the sailor.");
                return new UBoatDTO(UBoatStatus.PAYMENT_NOT_COMPLETED, false);
            }

            payment.complete();
            payment.getJourney().setState(JourneyState.PAYMENT_VERIFIED);
            paymentsRepository.save(payment);
            journeyRepository.save(payment.getJourney());
            log.info("The sailor has confirmed the cash payment.");
            return new UBoatDTO(UBoatStatus.PAYMENT_COMPLETED, true);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public UBoatDTO cardPay(String authorizationHeader) {
        try {
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            Optional<Journey> journeyOptional;
            if (account.getType() == UserType.CLIENT)
                journeyOptional = journeyRepository.findClientJourneyMatchingAccountAndState(account.getId(), Set.of(JourneyState.VERIFYING_PAYMENT));
            else
                journeyOptional = journeyRepository.findSailorJourneyMatchingAccountAndState(account.getId(), Set.of(JourneyState.VERIFYING_PAYMENT));

            if (journeyOptional.isEmpty())
                return new UBoatDTO(UBoatStatus.NO_JOURNEY_TO_PAY);

            var payment = journeyOptional.get().getPayment();
            return cardPayment(payment);
        } catch (Exception e) {
            log.error("An exception occurred during pay API workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatDTO cashPay(String authorizationHeader) {
        try {
            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            Optional<Journey> journeyOptional;
            if (account.getType() == UserType.CLIENT)
                journeyOptional = journeyRepository.findClientJourneyMatchingAccountAndState(account.getId(), Set.of(JourneyState.VERIFYING_PAYMENT));
            else
                journeyOptional = journeyRepository.findSailorJourneyMatchingAccountAndState(account.getId(), Set.of(JourneyState.VERIFYING_PAYMENT));

            if (journeyOptional.isEmpty())
                return new UBoatDTO(UBoatStatus.NO_JOURNEY_TO_PAY);

            var payment = journeyOptional.get().getPayment();
            return cashPayment(account, payment);
        } catch (Exception e) {
            log.error("An exception occurred during pay API workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Async
    public void triggerCardPayment(Payment payment) {

        try {
            if (payment.isCompleted()) {
                log.info("Payment already completed.");
                return;
            }

            if (payment.getPaymentType() == PaymentType.CASH) {
                log.warn("Automatic payment could not be engaged for payment with id {} due to the it being a cash payment.", payment.getId());
                return;
            }

            log.info("Automatic payment has been engaged for card payment with id {}.", payment.getId());
            cardPayment(payment);
        } catch (Exception e) {
            log.error("An exception occurred during pay workflow.", e);
        }
    }
}
