package com.uboat.vault.api.business.services.payment;

import com.uboat.vault.api.business.services.EntityService;
import com.uboat.vault.api.business.services.JwtService;
import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.domain.sailing.Payment;
import com.uboat.vault.api.model.dto.UBoatDTO;
import com.uboat.vault.api.model.enums.PaymentType;
import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.persistence.repostiories.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final EntityService entityService;
    private final JwtService jwtService;

    private final StripeService stripeService;

    private final PaymentsRepository paymentsRepository;

    private static final ReentrantLock lock = new ReentrantLock();

    @Transactional
    UBoatDTO pay(Account account, Payment payment) {
        lock.lock();
        try {
            if (payment.isCompleted() || payment.isPending()) {
                log.info("Payment already pending/completed.");
                return new UBoatDTO(UBoatStatus.PAYMENT_COMPLETED, true);
            }

            payment.setPending(true);
            paymentsRepository.save(payment);

            switch (payment.getPaymentType()) {
                case CASH: {
                    if (account.getType() == UserType.CLIENT) {
                        log.warn("A cash payment can only be confirmed by the sailor.");
                        return new UBoatDTO(UBoatStatus.PAYMENT_NOT_COMPLETED, false);
                    }

                    payment.complete();
                    log.info("The sailor has confirmed the cash payment.");
                    return new UBoatDTO(UBoatStatus.PAYMENT_COMPLETED, true);
                }
                case CARD: {

                    //todo call stripe API
                    log.info("TODO -> CARD PAYMENT VIA STRIPE");

                    payment.complete();
                    return new UBoatDTO(UBoatStatus.PAYMENT_COMPLETED, true);
                }
                default:
                    return new UBoatDTO(UBoatStatus.PAYMENT_NOT_COMPLETED, false);
            }

        } finally {
            payment.setPending(false);
            paymentsRepository.save(payment);
            lock.unlock();
        }
    }

    @Transactional
    public UBoatDTO pay(String authorizationHeader, Payment payment) {
        try {
            if (payment.isCompleted() || payment.isPending()) {
                log.info("Payment already pending/completed.");
                return new UBoatDTO(UBoatStatus.PAYMENT_COMPLETED, true);
            }

            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);
            return pay(account, payment);
        } catch (Exception e) {
            log.error("An exception occurred during pay workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Async
    public void triggerCardPayment(String authorizationHeader, Payment payment) {
        try {
            if (payment.isCompleted() || payment.isPending()) {
                log.info("Payment already pending/completed.");
                return;
            }

            if (payment.getPaymentType() == PaymentType.CASH) {
                log.warn("Automatic payment could not be engaged for payment with id {} due to the it being a cash payment.", payment.getId());
                return;
            }

            var jwtData = jwtService.extractUsernameAndPhoneNumberFromHeader(authorizationHeader);
            var account = entityService.findAccountByJwtData(jwtData);

            log.info("Automatic payment has been engaged for card payment with id {}.", payment.getId());
            pay(account, payment);
        } catch (Exception e) {
            log.error("An exception occurred during pay workflow.", e);
        }
    }
}
