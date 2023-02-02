package com.uboat.vault.api.business.services.sms.verification;

import com.uboat.vault.api.model.domain.account.account.Phone;
import com.uboat.vault.api.model.exceptions.SmsVerificationServiceException;

public interface SmsVerificationService {
    void sendRegistrationSms(Phone phone, Integer smsInteger) throws SmsVerificationServiceException;

    default String createMessage(Integer smsInteger) {
        return "Your SMS verification code is " + smsInteger;
    }
}
