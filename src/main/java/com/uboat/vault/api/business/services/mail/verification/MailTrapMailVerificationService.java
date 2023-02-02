package com.uboat.vault.api.business.services.mail.verification;

import com.uboat.vault.api.business.services.security.CryptoService;
import com.uboat.vault.api.persistence.repostiories.PendingAccountsRepository;
import com.uboat.vault.api.utilities.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

@Slf4j
@Service
@Primary
@Profile("development")
@RequiredArgsConstructor
public class MailTrapMailVerificationService implements MailVerificationService {
    @Autowired
    private Environment environment;
    @Value("${uboat.security.mailtrap_username}")
    private String username;
    @Value("${uboat.security.mailtrap_password}")
    private String password;

    @Value("${server.address}")
    private String serverAddress;

    @Value("${server.port}")
    private Integer port;
    private String uboatHostname;
    private final Properties prop = new Properties();

    private final CryptoService cryptoService;
    private final PendingAccountsRepository pendingAccountsRepository;

    @PostConstruct
    public void init() {
        log.warn("MailTrap Service initiated");
        if (Arrays.asList(environment.getActiveProfiles()).contains("production")) {
            prop.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
            prop.put("mail.smtp.port", "2525");
            prop.put("mail.smtp.auth", true);
            prop.put("mail.smtp.ssl.trust", "sandbox.smtp.mailtrap.io");
            prop.put("mail.smtp.starttls.enable", "yes");
            uboatHostname = "https://" + serverAddress;
        }
        if (Arrays.asList(environment.getActiveProfiles()).contains("development")) {
            prop.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
            prop.put("mail.smtp.port", "2525");
            prop.put("mail.smtp.auth", true);
            prop.put("mail.smtp.ssl.trust", "sandbox.smtp.mailtrap.io");
            prop.put("mail.smtp.starttls.enable", "yes");
            uboatHostname = "http://" + serverAddress + ':' + port;
        }
    }


    private Session getSession() {
        return Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Message generateMessage(Session session, String emailFrom, String emailTo, String subject, Multipart body) throws MessagingException {
        var message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailFrom));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo));
        message.setSubject(subject);
        message.setContent(body);
        return message;
    }

    private String replaceTokenFromHtmlWithHashedRToken(String html, String plaintextRToken) {
        var encodedHash = URLEncoder.encode(cryptoService.hash(plaintextRToken), StandardCharsets.UTF_8);
        var url = String.format("%s/api/verifyEmail?token=%s", uboatHostname, encodedHash);
        return html.replace("URL", url);
    }

    private String generateHtml(String registrationToken) throws IOException {
        var html = FileUtils.loadStaticHtmlTemplate("EmailVerificationTemplate.html");
        html = replaceTokenFromHtmlWithHashedRToken(html, registrationToken);
        return html;
    }

    private Multipart generateBody(String body) throws MessagingException {
        var mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(body, "text/html; charset=utf-8");
        var multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        return multipart;
    }

    @Transactional
    @Override
    public void sendRegistrationEmailConfirmationMail(String toEmail, String registrationToken) {
        try {
            var session = getSession();
            var body = generateBody(generateHtml(registrationToken));
            var message = generateMessage(session, "uboat@test.com", toEmail, "UBoat Mail Verification", body);
            Transport.send(message);
            this.updateEmailSentStatus(pendingAccountsRepository, registrationToken);
            log.info("Email to confirm account has been sent.");
        } catch (Exception e) {
            log.error("Exception occurred while trying to send email confirmation mail via MailTrap.", e);
        }
    }
}
