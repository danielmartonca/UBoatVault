package com.uboat.vault.api.business.services.mail.verification;

import org.springframework.scheduling.annotation.Async;

public interface MailVerificationService {
    @Async
    void sendRegistrationEmailConfirmationMail(String email,String username, String registrationToken);
}
