package com.uboat.vault.api.business.services.sms.verification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.uboat.vault.api.model.domain.account.account.Phone;
import com.uboat.vault.api.model.exceptions.SmsVerificationServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
@Primary
@Profile("production")
public class TwilioSmsVerificationService implements SmsVerificationService {
    @Value("${uboat.security.twilio_account_sid}")
    private String twilioAccountSid;

    @Value("${uboat.security.twilio_token}")
    private String twilioToken;
    @Value("${uboat.security.twilio_UBoat_phone_number}")
    private String twilioUBoatPhoneNumber;

    @PostConstruct
    public void init() {
        log.warn("Twilio Service initiated");
        Twilio.init(twilioAccountSid, twilioToken);
    }

    @Override
    public void sendRegistrationSms(Phone phone, Integer smsInteger) throws SmsVerificationServiceException {
        try {
            var message = Message.creator(
                            new com.twilio.type.PhoneNumber(phone.getNumber()),
                            new com.twilio.type.PhoneNumber(twilioUBoatPhoneNumber),
                            this.createMessage(smsInteger))
                    .create();
            log.info("Successfully sent registration SMS via twilio. Sid: {}", message.getSid());
        } catch (Exception e) {
            log.error("Failed to send sms via twilio service.", e);
            throw new SmsVerificationServiceException(e);
        }
    }
}
