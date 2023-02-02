package com.uboat.vault.api.business.services.sms.verification;

import com.uboat.vault.api.model.domain.account.account.Phone;
import com.uboat.vault.api.model.exceptions.SmsVerificationServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.telnet.TelnetClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintStream;

@Service
@Slf4j
@Profile("development")
public class MockSmsVerificationService implements SmsVerificationService {
    private void sendSmsToEmulatorUsingTelnet(Integer emulatorIp, Integer smsInteger) throws IOException {
        var telnet = new TelnetClient();
        telnet.connect("localhost", emulatorIp);

        var out = new PrintStream(telnet.getOutputStream());
        out.printf("sms send %s \"%s\"%n", "1234", this.createMessage(smsInteger));
        out.flush();

        out.print("quit");
        out.flush();

        telnet.disconnect();
    }

    @Override
    public void sendRegistrationSms(Phone phone, Integer smsInteger) throws SmsVerificationServiceException {
        try {
            if (phone.getNumber().endsWith("514")) sendSmsToEmulatorUsingTelnet(5554, smsInteger);
            if (phone.getNumber().endsWith("515")) sendSmsToEmulatorUsingTelnet(5555, smsInteger);
        } catch (Exception e) {
            log.error("Failed to send mock sms verification number.");
            throw new SmsVerificationServiceException(e);
        }
    }
}
