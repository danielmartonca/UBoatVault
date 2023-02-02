package com.uboat.vault.api.business.services.sms.verification;

import com.uboat.vault.api.model.domain.account.account.Phone;
import com.uboat.vault.api.model.exceptions.SmsVerificationServiceException;
import org.springframework.stereotype.Service;

@Service
public interface SmsVerificationService {
    void sendRegistrationSms(Phone phone, Integer smsInteger) throws SmsVerificationServiceException;
}
