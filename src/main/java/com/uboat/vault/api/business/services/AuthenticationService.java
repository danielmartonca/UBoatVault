package com.uboat.vault.api.business.services;

import com.uboat.vault.api.business.services.mail.verification.MailVerificationService;
import com.uboat.vault.api.business.services.sms.verification.SmsVerificationService;
import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.domain.account.account.Phone;
import com.uboat.vault.api.model.domain.account.account.RegistrationData;
import com.uboat.vault.api.model.domain.account.pending.PendingAccount;
import com.uboat.vault.api.model.domain.account.sailor.Sailor;
import com.uboat.vault.api.model.dto.AccountDTO;
import com.uboat.vault.api.model.dto.PhoneNumberDTO;
import com.uboat.vault.api.model.dto.UBoatDTO;
import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.persistence.repostiories.AccountsRepository;
import com.uboat.vault.api.persistence.repostiories.PendingAccountsRepository;
import com.uboat.vault.api.persistence.repostiories.RegistrationDataRepository;
import com.uboat.vault.api.persistence.repostiories.SailorsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtService jwtService;
    private final EntityService entityService;

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
        foundAccount = accountsRepository.findFirstByPhoneNumberAndPhoneDialCodeAndPhoneIsoCode(phoneNumber.getPhoneNumber(), phoneNumber.getDialCode(), phoneNumber.getIsoCode());
        if (foundAccount != null) {
            log.warn("Account with the given phone number already exists.");
            return true;
        }

        foundAccount = accountsRepository.findFirstByUsernameAndPassword(accountDto.getUsername(), accountDto.getPassword());
        if (foundAccount != null) {
            log.warn("Account with the given username and password already exist.");
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
    public UBoatDTO requestRegistration(AccountDTO account) {
        try {
            if (isAccountAlreadyExisting(account)) {
                log.warn("Account already exists with the given credentials.");
                return new UBoatDTO(UBoatStatus.ACCOUNT_ALREADY_EXISTS_BY_CREDENTIALS);
            }

            var pendingAccount = pendingAccountsRepository.findFirstByUsernameAndPassword(account.getUsername(), account.getPassword());
            if (pendingAccount != null) {
                var pendingRegistrationToken = pendingAccount.getToken();
                if (account.getEmail().equals(pendingAccount.getEmail())) {
                    pendingAccount.setEmail(account.getEmail());
                    pendingAccountsRepository.save(pendingAccount);
                }
                mailVerificationService.sendRegistrationEmailConfirmationMail(pendingAccount.getEmail(), pendingAccount.getUsername(), pendingRegistrationToken);
                return new UBoatDTO(UBoatStatus.ACCOUNT_ALREADY_PENDING_REGISTRATION, pendingRegistrationToken);
            }

            log.info("New pendingAccount and pendingToken will be generated.");

            //creating new pendingAccount and its pendingToken
            var registrationToken = generateRegistrationToken();

            var newPendingAccount = new PendingAccount(account, registrationToken);
            mailVerificationService.sendRegistrationEmailConfirmationMail(newPendingAccount.getEmail(), newPendingAccount.getUsername(), registrationToken);
            pendingAccountsRepository.save(newPendingAccount);

            log.info("Created new pending registration account and registrationToken. Returning registrationToken '" + registrationToken + "'.");
            return new UBoatDTO(UBoatStatus.ACCOUNT_REQUESTED_REGISTRATION_ACCEPTED, registrationToken);
        } catch (Exception e) {
            log.error("Error while requesting new registration.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    public UBoatDTO sendRegistrationSMS(PhoneNumberDTO phoneNumber, String rtoken, Integer smsInteger) {
        try {
            if (pendingAccountsRepository.findFirstByToken(rtoken) == null)
                return new UBoatDTO(UBoatStatus.RTOKEN_NOT_FOUND_IN_DATABASE, false);

            smsVerificationService.sendRegistrationSms(phoneNumber, smsInteger);
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
    public UBoatDTO register(AccountDTO accountDTO, String registrationToken) {
        try {
            var pendingAccount = pendingAccountsRepository.findFirstByToken(registrationToken);
            if (pendingAccount == null) {
                log.warn("There is no matching pending registrationToken to the provided registrationToken: " + registrationToken);
                return new UBoatDTO(UBoatStatus.RTOKEN_NOT_FOUND_IN_DATABASE);
            }

            pendingAccount = pendingAccountsRepository.findFirstByUsernameAndPassword(accountDTO.getUsername(), accountDTO.getPassword());
            if (pendingAccount == null || !accountDTO.equalsPendingAccount(pendingAccount)) {
                log.warn("Token found but credentials don't match.");
                return new UBoatDTO(UBoatStatus.RTOKEN_AND_ACCOUNT_NOT_MATCHING);
            }

            if (accountDTO.getRegistrationData() == null || accountDTO.getPhoneNumber() == null) {
                log.warn("Account request is missing registrationData or phoneNumber");
                return new UBoatDTO(UBoatStatus.MISSING_REGISTRATION_DATA_OR_PHONE_NUMBER);
            }

            var account = new Account(accountDTO);

            var registrationData = registrationDataRepository.findFirstByDeviceInfo(accountDTO.getRegistrationData().getDeviceInfo());

            if (registrationData == null) registrationData = new RegistrationData(accountDTO.getRegistrationData());
            else
                log.warn("Registration data is already used by another account. There will be two accounts bound to this device.");
            account.setRegistrationData(registrationData);

            var jsonWebToken = jwtService.generateJwt(account.getPhone().getNumber(), account.getUsername(), account.getPassword());

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
    public UBoatDTO login(AccountDTO account) {
        try {
            var foundAccountsList = accountsRepository.findAllByPassword(account.getPassword());
            if (foundAccountsList == null) {
                log.warn("No account was found with the given username/phone number and password.");
                return new UBoatDTO(UBoatStatus.CREDENTIALS_NOT_FOUND);
            }

            for (var foundAccount : foundAccountsList) {
                if (foundAccount.getUsername().equals(account.getUsername()) || foundAccount.getPhone().equals(new Phone(account.getPhoneNumber()))) {
                    log.info("Credentials matched. Found account.");
                    var jwt = jwtService.generateJwt(account.getPhoneNumber().getPhoneNumber(), account.getUsername(), account.getPassword());
                    return new UBoatDTO(UBoatStatus.LOGIN_SUCCESSFUL, jwt);
                } else log.warn("An account with given password found but neither username or phone number match.");
            }

            log.warn("Invalid credentials. Login failed");
            return new UBoatDTO(UBoatStatus.INVALID_CREDENTIALS);
        } catch (Exception e) {
            log.error("An exception occurred during login workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }
}
