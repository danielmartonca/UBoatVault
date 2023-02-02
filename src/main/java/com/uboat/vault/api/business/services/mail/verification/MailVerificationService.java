package com.uboat.vault.api.business.services.mail.verification;

import com.uboat.vault.api.persistence.repostiories.PendingAccountsRepository;
import org.springframework.scheduling.annotation.Async;

public interface MailVerificationService {

    @Async
    void sendRegistrationEmailConfirmationMail(String toEmail, String registrationToken);

    default void completeEmailVerification(PendingAccountsRepository pendingAccountsRepository, String registrationToken) {
        var account = pendingAccountsRepository.findFirstByToken(registrationToken);
        if (account == null) return;

        if (!account.isEmailVerified()) {
            account.setEmailVerified(true);
            pendingAccountsRepository.save(account);
        }
    }

    default void updateEmailSentStatus(PendingAccountsRepository pendingAccountsRepository, String registrationToken) {
        var account = pendingAccountsRepository.findFirstByToken(registrationToken);
        if (account == null) return;

        if (!account.isEmailSent()) {
            account.setEmailSent(true);
            pendingAccountsRepository.save(account);
        }
    }
}
