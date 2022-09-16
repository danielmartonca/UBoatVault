package com.uboat.vault.api.services;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.http.UBoatResponse;
import com.uboat.vault.api.model.http.new_requests.RequestAccount;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.account.info.RegistrationData;
import com.uboat.vault.api.model.persistence.account.pending.PendingAccount;
import com.uboat.vault.api.model.persistence.account.pending.PendingToken;
import com.uboat.vault.api.model.persistence.sailing.sailor.Boat;
import com.uboat.vault.api.model.persistence.sailing.sailor.Sailor;
import com.uboat.vault.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthenticationService {
    private final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final JwtService jwtService;
    private final EntityService entityService;

    private final AccountsRepository accountsRepository;
    private final RegistrationDataRepository registrationDataRepository;
    private final PendingTokenRepository pendingTokenRepository;
    private final PendingAccountsRepository pendingAccountsRepository;
    private final PhoneNumbersRepository phoneNumbersRepository;
    private final SailorsRepository sailorsRepository;

    private final Pattern phoneNumberPattern = Pattern.compile("^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$");
    private final Pattern usernamePattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.-]*$");
//    private final Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    @Autowired
    public AuthenticationService(RegistrationDataRepository registrationDataRepository, JwtService jwtService, EntityService entityService, PendingTokenRepository pendingTokenRepository, PendingAccountsRepository pendingAccountsRepository, AccountsRepository accountsRepository, PhoneNumbersRepository phoneNumbersRepository, SailorsRepository sailorsRepository) {
        this.registrationDataRepository = registrationDataRepository;
        this.jwtService = jwtService;
        this.entityService = entityService;
        this.pendingTokenRepository = pendingTokenRepository;
        this.pendingAccountsRepository = pendingAccountsRepository;
        this.accountsRepository = accountsRepository;
        this.phoneNumbersRepository = phoneNumbersRepository;
        this.sailorsRepository = sailorsRepository;
    }

    public UBoatResponse checkUsername(String username) {
        var matcher = usernamePattern.matcher(username);
        if (!matcher.matches()) {
            log.info("Username doesn't match pattern.");
            return new UBoatResponse(UBoatStatus.USERNAME_INVALID_FORMAT, null);
        }
        var isAlreadyUsed = entityService.isUsernameUsed(username);
        if (isAlreadyUsed) {
            log.info("Username '" + username + "' is already used.");
            return new UBoatResponse(UBoatStatus.USERNAME_ALREADY_USED, true);
        }

        log.info("Username '" + username + "' is not used.");
        return new UBoatResponse(UBoatStatus.USERNAME_ACCEPTED, false);
    }

    public UBoatResponse checkPhoneNumber(String phoneNumber, String dialCode, String isoCode) {
        var matcher = phoneNumberPattern.matcher(phoneNumber);
        if (!matcher.matches()) {
            log.warn("Phone number doesn't match pattern.");
            return new UBoatResponse(UBoatStatus.PHONE_NUMBER_INVALID_FORMAT, null);
        }
        if (dialCode.length() > 5 || isoCode.length() >= 3) {
            log.warn("Dial code or iso code too long.");
            return null;
        }

        var isAlreadyUsed = entityService.isPhoneNumberUsed(phoneNumber, dialCode, isoCode);
        if (isAlreadyUsed) {
            log.info("Phone number '" + phoneNumber + "' is already used.");
            return new UBoatResponse(UBoatStatus.PHONE_NUMBER_ALREADY_USED, true);
        }

        log.info("Phone number '" + phoneNumber + "' is not used.");
        return new UBoatResponse(UBoatStatus.PHONE_NUMBER_ACCEPTED, false);
    }

    private boolean isAccountAlreadyExisting(RequestAccount account) {
        Account foundAccount;
        foundAccount = accountsRepository.findFirstByUsername(account.getUsername());
        if (foundAccount != null) {
            log.warn("Account with the given username already exists.");
            return true;
        }

        foundAccount = accountsRepository.findFirstByUsernameAndPassword(account.getUsername(), account.getPassword());
        if (foundAccount != null) {
            log.warn("Account with the given username and password already exist.");
            return true;
        }

        var phoneNumber = account.getPhoneNumber();
        var foundPhoneNumber = phoneNumbersRepository.findFirstByPhoneNumberAndDialCodeAndIsoCode(phoneNumber.getPhoneNumber(), phoneNumber.getDialCode(), phoneNumber.getIsoCode());
        if (foundPhoneNumber != null) {
            log.warn("Account with the given phone number already exists.");
            return true;
        }
        log.info("No account in the database with the given credentials found. Check passed.");
        return false;
    }

    private boolean isPendingAccountAlreadyExisting(RequestAccount account) {
        try {
            PendingAccount foundAccount = pendingAccountsRepository.findFirstByUsernameAndPassword(account.getUsername(), account.getPassword());
            return foundAccount != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String getPendingAccountToken(RequestAccount account) {
        var pendingToken = pendingTokenRepository.findFirstByAccount_UsernameAndAccount_Password(account.getUsername(), account.getPassword());
        if (pendingToken == null) return null;
        return pendingToken.getTokenValue();
    }

    @Transactional
    public String generateRegistrationToken() {
        String token;
        do {
            UUID uuid = UUID.randomUUID();
            token = uuid.toString();
            if (pendingTokenRepository.findFirstByTokenValue(token) != null) token = "";
        } while (token.equals(""));
        return token;
    }

    /**
     * This method returns a new token if registrationData is not found in database, or its token if it is found.
     */
    @Transactional
    public UBoatResponse requestRegistration(RequestAccount account) {
        try {
            if (isAccountAlreadyExisting(account)) {
                log.warn("Account already exists with the given credentials.");
                return new UBoatResponse(UBoatStatus.ACCOUNT_ALREADY_EXISTS_BY_CREDENTIALS);
            }

            if (isPendingAccountAlreadyExisting(account)) {
                log.warn("Pending account already exists returning the registrationToken.");
                var pendingRegistrationToken = getPendingAccountToken(account);
                return new UBoatResponse(UBoatStatus.ACCOUNT_ALREADY_PENDING_REGISTRATION, pendingRegistrationToken);
            }

            log.info("New pendingAccount and pendingToken will be generated.");

            //creating new pendingAccount and its pendingToken
            var registrationToken = generateRegistrationToken();

            var newPendingAccount = new PendingAccount(account);
            var newPendingToken = new PendingToken(registrationToken);
            newPendingAccount.setPendingToken(newPendingToken);
            newPendingToken.setAccount(newPendingAccount);

            pendingAccountsRepository.save(newPendingAccount);
            log.info("Created new pending registration account and registrationToken. Returning registrationToken '" + registrationToken + "'.");
            return new UBoatResponse(UBoatStatus.ACCOUNT_REQUESTED_REGISTRATION_ACCEPTED, registrationToken);
        } catch (Exception e) {
            log.error("Error while requesting new registration.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    private void createSailor(Account account) {
        if (account.getType() != UserType.SAILOR) {
            log.error("Account given as parameter is not a sailor account");
            throw new RuntimeException("Account given as parameter is not a sailor account");
        }

        var sailor = Sailor.builder()
                .accountId(account.getId())
                .averageRating(0)
                .rankings(new HashSet<>())
                .build();
        sailor.setBoat(new Boat(sailor));

        sailorsRepository.save(sailor);
        log.info("Successfully created sailor entity.");
    }

    @Transactional
    public UBoatResponse register(RequestAccount requestAccount, String registrationToken) {
        try {
            var pendingToken = pendingTokenRepository.findFirstByTokenValue(registrationToken);
            if (pendingToken == null) {
                log.warn("There is no matching pending registrationToken to the provided registrationToken: " + registrationToken);
                return new UBoatResponse(UBoatStatus.RTOKEN_NOT_FOUND_IN_DATABASE);
            }

            var pendingAccount = pendingAccountsRepository.findFirstByUsernameAndPassword(requestAccount.getUsername(), requestAccount.getPassword());
            if (pendingAccount == null || !requestAccount.equalsPendingAccount(pendingAccount)) {
                log.warn("Token found but credentials don't match.");
                return new UBoatResponse(UBoatStatus.RTOKEN_AND_ACCOUNT_NOT_MATCHING);
            }

            if (requestAccount.getRegistrationData() == null || requestAccount.getPhoneNumber() == null) {
                log.warn("Account request is missing registrationData or phoneNumber");
                return new UBoatResponse(UBoatStatus.MISSING_REGISTRATION_DATA_OR_PHONE_NUMBER);
            }

            var account = new Account(requestAccount);

            var registrationData = registrationDataRepository.findFirstByDeviceInfo(requestAccount.getRegistrationData().getDeviceInfo());

            if (registrationData == null)
                registrationData = new RegistrationData(requestAccount.getRegistrationData());
            else
                log.warn("Registration data is already used by another account. There will be two accounts bound to this device.");
            account.setRegistrationData(registrationData);

            var jsonWebToken = jwtService.generateJwt(account.getPhoneNumber().getPhoneNumber(), account.getUsername(), account.getPassword());

            account = accountsRepository.save(account);
            pendingAccountsRepository.delete(pendingAccount);

            if (account.getType() == UserType.SAILOR)
                createSailor(account);

            log.info("Registration successful. Returning JWT.");
            return new UBoatResponse(UBoatStatus.REGISTRATION_SUCCESSFUL, jsonWebToken);
        } catch (Exception e) {
            log.error("Exception occurred while registering.", e);
            return new UBoatResponse(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UBoatResponse login(RequestAccount account) {
        var foundAccountsList = accountsRepository.findAllByPassword(account.getPassword());
        if (foundAccountsList == null) {
            log.warn("No account was found with the given username/phone number and password.");
            return new UBoatResponse(UBoatStatus.CREDENTIALS_NOT_FOUND);
        }

        for (var foundAccount : foundAccountsList) {
            if (foundAccount.getUsername().equals(account.getUsername()) ||
                    foundAccount.getPhoneNumber().equals(account.getPhoneNumber())) {
                log.info("Credentials matched. Found account.");
                var jwt = jwtService.generateJwt(account.getPhoneNumber().getPhoneNumber(), account.getUsername(), account.getPassword());
                return new UBoatResponse(UBoatStatus.LOGIN_SUCCESSFUL, jwt);
            } else
                log.warn("An account with given password found but neither username or phone number match.");
        }
        log.warn("Invalid credentials. Login failed");
        return new UBoatResponse(UBoatStatus.INVALID_CREDENTIALS);
    }
}
