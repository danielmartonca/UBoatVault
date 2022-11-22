package com.uboat.vault.api.business.services.mail.verification;

import com.uboat.vault.api.persistence.repostiories.PendingAccountsRepository;
import com.uboat.vault.api.persistence.repostiories.PendingTokenRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class MockMailVerificationService implements MailVerificationService {

    private final PendingTokenRepository pendingTokenRepository;
    private final PendingAccountsRepository pendingAccountsRepository;

    /**
     * Currently waits between 10-20 seconds and sets the flag as true after the timer has finished.
     */
    @Transactional
    @Override
    public void sendRegistrationEmailConfirmationMail(String email, String username, String registrationToken) {
        try {
            log.warn("Mail Confirmation Service not implemented.");
            var seconds = (long) (Math.random() * (20 - 10) + 10);
            log.warn("Waiting " + seconds + " seconds until automatically settings the email verification flag to true.");

            TimeUnit.SECONDS.sleep((long) (Math.random() * (20 - 10) + 10));

            var pendingToken = pendingTokenRepository.findFirstByTokenValue(registrationToken);
            if (pendingToken == null) return;

            var account = pendingToken.getAccount();
            if (account == null) return;

            if (!account.isEmailVerified()) {
                account.setEmailVerified(true);
                pendingAccountsRepository.save(account);
                log.info("Verified email automatically.");
            } else
                log.info("Email is already verified.");
        } catch (Exception e) {
            log.error("Exception occurred while trying to send email confirmation mail.", e);
        }
    }
}
