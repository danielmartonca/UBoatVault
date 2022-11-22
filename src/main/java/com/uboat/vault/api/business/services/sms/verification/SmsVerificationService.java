package com.uboat.vault.api.business.services.sms.verification;

import com.uboat.vault.api.model.dto.PhoneNumberDTO;
import com.uboat.vault.api.model.exceptions.SmsVerificationServiceException;
import org.springframework.stereotype.Service;

@Service
public interface SmsVerificationService {
    void sendRegistrationSms(PhoneNumberDTO phoneNumber, Integer smsInteger) throws SmsVerificationServiceException;
}
