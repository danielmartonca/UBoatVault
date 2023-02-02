package com.uboat.vault.api.business.services;

import com.uboat.vault.api.business.services.mail.verification.MailVerificationService;
import com.uboat.vault.api.business.services.security.CryptoService;
import com.uboat.vault.api.business.services.sms.verification.MockSmsVerificationService;
import com.uboat.vault.api.business.services.sms.verification.SmsVerificationService;
import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.domain.account.account.RegistrationData;
import com.uboat.vault.api.model.domain.account.pending.PendingAccount;
import com.uboat.vault.api.model.domain.account.sailor.Sailor;
import com.uboat.vault.api.model.dto.AccountDTO;
import com.uboat.vault.api.model.dto.UBoatDTO;
import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.persistence.repostiories.AccountsRepository;
import com.uboat.vault.api.persistence.repostiories.PendingAccountsRepository;
import com.uboat.vault.api.persistence.repostiories.RegistrationDataRepository;
import com.uboat.vault.api.persistence.repostiories.SailorsRepository;
import com.uboat.vault.api.utilities.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtService jwtService;
    private final EntityService entityService;

    private final CryptoService cryptoService;

    private final AccountsRepository accountsRepository;
    private final RegistrationDataRepository registrationDataRepository;
    private final PendingAccountsRepository pendingAccountsRepository;
    private final SailorsRepository sailorsRepository;
    private final SmsVerificationService smsVerificationService;
    private final MailVerificationService mailVerificationService;

    @Value("${uboat.regex.phone}")
    private String phoneNumberPattern;
    @Value("${uboat.regex.username}")
    private String usernamePattern;
    @Value("${uboat.regex.email}")
    private String emailPattern;

    public UBoatDTO checkEmail(String email) {
        var matcher = Pattern.compile(emailPattern).matcher(email);
        if (!matcher.matches()) {
            log.info("Email doesn't match pattern.");
            return new UBoatDTO(UBoatStatus.EMAIL_INVALID_FORMAT, null);
        }

        var isAlreadyUsed = entityService.isEmailUsed(email);
        if (isAlreadyUsed) {
            log.info("Email '" + email + "' is already used.");
            return new UBoatDTO(UBoatStatus.EMAIL_ALREADY_USED, true);
        }

        log.info("Email '" + email + "' is not used.");
        return new UBoatDTO(UBoatStatus.EMAIL_ACCEPTED, false);
    }

    public UBoatDTO checkUsername(String username) {
        var matcher = Pattern.compile(usernamePattern).matcher(username);
        if (!matcher.matches()) {
            log.info("Username doesn't match pattern.");
            return new UBoatDTO(UBoatStatus.USERNAME_INVALID_FORMAT, null);
        }
        var isAlreadyUsed = entityService.isUsernameUsed(username);
        if (isAlreadyUsed) {
            log.info("Username '" + username + "' is already used.");
            return new UBoatDTO(UBoatStatus.USERNAME_ALREADY_USED, true);
        }

        log.info("Username '" + username + "' is not used.");
        return new UBoatDTO(UBoatStatus.USERNAME_ACCEPTED, false);
    }

    public UBoatDTO checkPhoneNumber(String phoneNumber, String dialCode, String isoCode) {
        var matcher = Pattern.compile(phoneNumberPattern).matcher(phoneNumber);
        if (!matcher.matches()) {
            log.warn("Phone number doesn't match pattern.");
            return new UBoatDTO(UBoatStatus.PHONE_NUMBER_INVALID_FORMAT, null);
        }
        if (dialCode.length() > 5 || isoCode.length() >= 3) {
            log.warn("Dial code or iso code too long.");
            return null;
        }

        var isAlreadyUsed = entityService.isPhoneNumberUsed(phoneNumber, dialCode, isoCode);
        if (isAlreadyUsed) {
            log.info("Phone number '" + phoneNumber + "' is already used.");
            return new UBoatDTO(UBoatStatus.PHONE_NUMBER_ALREADY_USED, true);
        }

        log.info("Phone number '" + phoneNumber + "' is not used.");
        return new UBoatDTO(UBoatStatus.PHONE_NUMBER_ACCEPTED, false);
    }

    private boolean isAccountAlreadyExisting(AccountDTO accountDto) {
        var foundAccount = accountsRepository.findFirstByUsername(accountDto.getUsername());
        if (foundAccount != null) {
            log.warn("Account with the given username already exists.");
            return true;
        }

        var phoneNumber = accountDto.getPhoneNumber();
        foundAccount = accountsRepository.findFirstByPhoneNumberAndPhoneDialCodeAndPhoneIsoCode(phoneNumber.getNumber(), phoneNumber.getDialCode(), phoneNumber.getIsoCode());
        if (foundAccount != null) {
            log.warn("Account with the given phone number already exists.");
            return true;
        }

        log.info("No account in the database with the given credentials found.");
        return false;
    }

    @Transactional
    public String generateRegistrationToken() {
        String token;
        do {
            UUID uuid = UUID.randomUUID();
            token = uuid.toString();
            if (pendingAccountsRepository.findFirstByToken(token) != null) token = "";
        } while (token.equals(""));
        return token;
    }

    /**
     * This method returns a new token if registrationData is not found in database, or its token if it is found.
     */
    @Transactional
    public UBoatDTO requestRegistration(AccountDTO dto) {
        try {
            if (isAccountAlreadyExisting(dto)) {
                log.warn("Account already exists with the given credentials.");
                return new UBoatDTO(UBoatStatus.ACCOUNT_ALREADY_EXISTS_BY_CREDENTIALS);
            }

            var pendingAccount = pendingAccountsRepository.findFirstByUsername(dto.getUsername());

            if (pendingAccount == null) {
                var phoneNumber = dto.getPhoneNumber();
                pendingAccount = pendingAccountsRepository.findFirstByPhoneNumberAndPhoneDialCodeAndPhoneIsoCode(phoneNumber.getNumber(), phoneNumber.getDialCode(), phoneNumber.getIsoCode());
            }

            if (pendingAccount != null) {
                if (dto.getUsername().equals(pendingAccount.getUsername()) ||
                        !dto.getPhoneNumber().equals(pendingAccount.getPhone()) ||
                        !cryptoService.matchesHash(dto.getPassword(), pendingAccount.getPassword())) {
                    return new UBoatDTO(UBoatStatus.ACCOUNT_ALREADY_EXISTS_BY_CREDENTIALS, null);
                }

                var pendingRegistrationToken = pendingAccount.getToken();
                if (dto.getEmail().equals(pendingAccount.getEmail())) {
                    pendingAccount.setEmail(dto.getEmail());
                    pendingAccountsRepository.save(pendingAccount);
                }

                mailVerificationService.sendRegistrationEmailConfirmationMail(pendingAccount.getEmail(), pendingAccount.getUsername(), pendingRegistrationToken);
                return new UBoatDTO(UBoatStatus.ACCOUNT_ALREADY_PENDING_REGISTRATION, pendingRegistrationToken);
            }

            log.info("New pendingAccount and pendingToken will be generated.");

            //creating new pendingAccount and its pendingToken
            var registrationToken = generateRegistrationToken();

            dto.setPassword(cryptoService.hash(dto.getPassword()));
            var newPendingAccount = new PendingAccount(dto, registrationToken);
            mailVerificationService.sendRegistrationEmailConfirmationMail(newPendingAccount.getEmail(), newPendingAccount.getUsername(), registrationToken);
            pendingAccountsRepository.save(newPendingAccount);

            log.info("Created new pending registration account and registrationToken. Returning registrationToken '" + registrationToken + "'.");
            return new UBoatDTO(UBoatStatus.ACCOUNT_REQUESTED_REGISTRATION_ACCEPTED, registrationToken);
        } catch (Exception e) {
            log.error("Error while requesting new registration.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    public UBoatDTO sendRegistrationSMS(String rtoken, Integer smsInteger) {
        try {
            var pendingAccount = pendingAccountsRepository.findFirstByToken(rtoken);
            if (pendingAccount == null)
                return new UBoatDTO(UBoatStatus.RTOKEN_NOT_FOUND_IN_DATABASE, false);

            if (smsVerificationService instanceof MockSmsVerificationService) //mock can spam the phone number
            {
                log.warn("Mock SMS service detected");
                smsVerificationService.sendRegistrationSms(pendingAccount.getPhone(), smsInteger);
                pendingAccount.setLastSmsSentDate(new Date());
            } else if (pendingAccount.getLastSmsSentDate() == null || DateUtils.getSecondsPassed(pendingAccount.getLastSmsSentDate()) >= 60) //a real service can send it every minute
            {
                smsVerificationService.sendRegistrationSms(pendingAccount.getPhone(), smsInteger);
                pendingAccount.setLastSmsSentDate(new Date());
            }

            return new UBoatDTO(UBoatStatus.REGISTRATION_SMS_SENT, true);
        } catch (Exception e) {
            log.error("Error while sending the registration SMS.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    public UBoatDTO emailVerification(String registrationToken) {
        try {
            var pendingAccount = pendingAccountsRepository.findFirstByToken(registrationToken);
            if (pendingAccount == null) {
                log.warn("There is no matching pending registrationToken to the provided registrationToken: " + registrationToken);
                return new UBoatDTO(UBoatStatus.RTOKEN_NOT_FOUND_IN_DATABASE, false);
            }

            if (!pendingAccount.isEmailVerified())
                return new UBoatDTO(UBoatStatus.EMAIL_NOT_VERIFIED, false);

            return new UBoatDTO(UBoatStatus.EMAIL_VERIFIED, true);
        } catch (Exception e) {
            log.error("Error while checking if the email is verified.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    private void createSailor(Account account) {
        var sailor = new Sailor(account);
        sailorsRepository.save(sailor);
        log.info("Successfully created sailor entity.");
    }

    @Transactional
    public UBoatDTO register(AccountDTO dto, String registrationToken) {
        try {
            var pendingAccount = pendingAccountsRepository.findFirstByToken(registrationToken);
            if (pendingAccount == null) {
                log.warn("There is no matching pending registrationToken to the provided registrationToken: " + registrationToken);
                return new UBoatDTO(UBoatStatus.RTOKEN_NOT_FOUND_IN_DATABASE);
            }

            pendingAccount = pendingAccountsRepository.findFirstByUsername(dto.getUsername());

            if (pendingAccount == null || !pendingAccount.getUsername().equals(dto.getUsername()) || !cryptoService.matchesHash(dto.getPassword(), pendingAccount.getPassword())) {
                log.warn("Token found but credentials don't match.");
                return new UBoatDTO(UBoatStatus.RTOKEN_AND_ACCOUNT_NOT_MATCHING);
            }

            if (dto.getRegistrationData() == null || dto.getPhoneNumber() == null) {
                log.warn("Account request is missing registrationData or phoneNumber");
                return new UBoatDTO(UBoatStatus.MISSING_REGISTRATION_DATA_OR_PHONE_NUMBER);
            }

            dto.setPassword(cryptoService.hash(dto.getPassword()));
            var account = new Account(dto);

            var registrationData = registrationDataRepository.findFirstByDeviceInfo(dto.getRegistrationData().getDeviceInfo());

            if (registrationData == null) registrationData = new RegistrationData(dto.getRegistrationData());
            else
                log.warn("Registration data is already used by another account. There will be two accounts bound to this device.");
            account.setRegistrationData(registrationData);

            var jsonWebToken = jwtService.generateJwt(account);

            account = accountsRepository.save(account);
            pendingAccountsRepository.delete(pendingAccount);

            if (account.getType() == UserType.SAILOR) createSailor(account);

            log.info("Registration successful. Returning JWT.");
            return new UBoatDTO(UBoatStatus.REGISTRATION_SUCCESSFUL, jsonWebToken);
        } catch (Exception e) {
            log.error("Exception occurred while registering.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatDTO login(AccountDTO dto) {
        try {
            var foundAccount = accountsRepository.findFirstByUsername(dto.getUsername());

            if (foundAccount == null) {
                var phoneNumber = dto.getPhoneNumber();
                foundAccount = accountsRepository.findFirstByPhoneNumberAndPhoneDialCodeAndPhoneIsoCode(phoneNumber.getNumber(), phoneNumber.getDialCode(), phoneNumber.getIsoCode());
            }

            if (foundAccount == null) {
                log.warn("No account was found with the given username/phone number.");
                return new UBoatDTO(UBoatStatus.CREDENTIALS_NOT_FOUND);
            }

            if (!cryptoService.matchesHash(dto.getPassword(), foundAccount.getPassword())) {
                log.warn("Invalid credentials. Login failed");
                return new UBoatDTO(UBoatStatus.INVALID_CREDENTIALS);
            }

            log.info("Credentials matched. Found account.");
            var jwt = jwtService.generateJwt(foundAccount);
            return new UBoatDTO(UBoatStatus.LOGIN_SUCCESSFUL, jwt);
        } catch (Exception e) {
            log.error("An exception occurred during login workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }
}
