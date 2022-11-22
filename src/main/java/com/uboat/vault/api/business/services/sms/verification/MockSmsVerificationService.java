package com.uboat.vault.api.business.services.sms.verification;

import com.uboat.vault.api.model.dto.PhoneNumberDTO;
import com.uboat.vault.api.model.exceptions.SmsVerificationServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MockSmsVerificationService implements SmsVerificationService {
    @Override
    public void sendRegistrationSms(PhoneNumberDTO phoneNumber, Integer smsInteger) throws SmsVerificationServiceException {
        log.warn("SMS Service not implemented.");
    }
}
