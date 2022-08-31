package com.example.uboatvault.api.services;

import com.example.uboatvault.api.model.enums.UserType;
import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.account.info.PhoneNumber;
import com.example.uboatvault.api.model.persistence.account.info.RegistrationData;
import com.example.uboatvault.api.model.persistence.account.pending.PendingAccount;
import com.example.uboatvault.api.model.persistence.account.pending.PendingToken;
import com.example.uboatvault.api.model.persistence.sailing.sailor.ActiveSailor;
import com.example.uboatvault.api.model.persistence.sailing.sailor.Boat;
import com.example.uboatvault.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Matcher;
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
    private final ActiveSailorsRepository activeSailorsRepository;

    private final Pattern phoneNumberPattern = Pattern.compile("^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$");
    private final Pattern usernamePattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.-]*$");
//    private final Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    @Autowired
    public AuthenticationService(RegistrationDataRepository registrationDataRepository, JwtService jwtService, EntityService entityService, PendingTokenRepository pendingTokenRepository, PendingAccountsRepository pendingAccountsRepository, AccountsRepository accountsRepository, PhoneNumbersRepository phoneNumbersRepository, ActiveSailorsRepository activeSailorsRepository) {
        this.registrationDataRepository = registrationDataRepository;
        this.jwtService = jwtService;
        this.entityService = entityService;
        this.pendingTokenRepository = pendingTokenRepository;
        this.pendingAccountsRepository = pendingAccountsRepository;
        this.accountsRepository = accountsRepository;
        this.phoneNumbersRepository = phoneNumbersRepository;
        this.activeSailorsRepository = activeSailorsRepository;
    }

    public Boolean checkUsername(String username) {
        Matcher m = usernamePattern.matcher(username);
        if (!m.matches()) {
            log.info("Username doesn't match pattern.");
            return null;
        }
        var isAlreadyUsed = entityService.isUsernameUsed(username);
        if (isAlreadyUsed)
            log.info("Username '" + username + "' is already used.");
        else
            log.info("Username '" + username + "' is not used.");
        return isAlreadyUsed;
    }

    public Boolean checkPhoneNumber(String phoneNumber, String dialCode, String isoCode) {
        Matcher m = phoneNumberPattern.matcher(phoneNumber);
        if (!m.matches()) {
            log.warn("Phone number doesn't match pattern.");
            return null;
        }
        if (dialCode.length() > 5 || isoCode.length() >= 3) {
            log.warn("Dial code or iso code too long.");
            return null;
        }

        var isAlreadyUsed = entityService.isPhoneNumberUsed(phoneNumber, dialCode, isoCode);
        if (isAlreadyUsed)
            log.info("Phone number '" + phoneNumber + "' is already used.");
        else
            log.info("Phone number '" + phoneNumber + "' is not used.");
        return isAlreadyUsed;
    }

    private boolean isAccountAlreadyExisting(Account account) {
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

        PhoneNumber phoneNumber = account.getPhoneNumber();
        PhoneNumber foundPhoneNumber = phoneNumbersRepository.findFirstByPhoneNumberAndDialCodeAndIsoCode(phoneNumber.getPhoneNumber(), phoneNumber.getDialCode(), phoneNumber.getIsoCode());
        if (foundPhoneNumber != null) {
            log.warn("Account with the given phone number already exists.");
            return true;
        }
        log.info("No account in the database with the given credentials found. Check passed.");
        return false;
    }

    private boolean isPendingAccountAlreadyExisting(Account account) {
        try {
            PendingAccount foundAccount = pendingAccountsRepository.findFirstByUsernameAndPassword(account.getUsername(), account.getPassword());
            return foundAccount != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String getPendingAccountToken(Account account) {
        var pendingToken = pendingTokenRepository.findFirstByAccount_UsernameAndAccount_Password(account.getUsername(), account.getPassword());
        if (pendingToken == null) return null;
        return pendingToken.getTokenValue();
    }

    @Transactional
    String generateRegistrationToken() {
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
    public String requestRegistration(Account account) {
        try {
            if (isAccountAlreadyExisting(account)) {
                log.warn("Account already exists with the given credentials.");
                return null;
            }

            if (isPendingAccountAlreadyExisting(account)) {
                log.warn("Pending account already exists returning the registrationToken.");
                return getPendingAccountToken(account);
            }

            log.info("New pendingAccount and pendingToken will be generated.");

            //creating new pendingAccount and it's pendingToken
            String registrationToken = generateRegistrationToken();
            var newPendingAccount = new PendingAccount(account);
            PendingToken newPendingToken = new PendingToken(registrationToken);
            newPendingAccount.setPendingToken(newPendingToken);
            newPendingToken.setAccount(newPendingAccount);

            pendingAccountsRepository.save(newPendingAccount);
            log.info("Created new pending registration account and registrationToken. Returning registrationToken '" + registrationToken + "'.");
            return registrationToken;
        } catch (Exception e) {
            log.error("Error while requesting new registration.", e);
            return null;
        }
    }

    private void createActiveSailorAccount(Account account) {
        if (account.getType() == UserType.SAILOR) {
            var boat = new Boat();
            var activeSailor = ActiveSailor.builder()
                    .accountId(accountsRepository.findFirstByUsernameAndPassword(account.getUsername(), account.getPassword()).getId())
                    .boat(boat)
                    .averageRating(0)
                    .build();
            boat.setSailor(activeSailor);
            activeSailorsRepository.save(activeSailor);
            log.info("Successfully created active sailor entity.");
        } else {
            log.error("Account given as parameter is not a sailor account");
            throw new RuntimeException("Account given as parameter is not a sailor account");
        }
    }

    @Transactional
    public String register(Account requestAccount, String registrationToken) {
        try {
            PendingToken pendingToken = pendingTokenRepository.findFirstByTokenValue(registrationToken);
            if (pendingToken == null) {
                log.warn("There is no matching pending registrationToken to the provided registrationToken: " + registrationToken);
                return null;
            }

            if (!registrationToken.equals(pendingToken.getTokenValue())) {
                log.warn("Tokens don't match.");
                return null;
            }

            PendingAccount pendingAccount = pendingAccountsRepository.findFirstByUsernameAndPassword(requestAccount.getUsername(), requestAccount.getPassword());
            if (pendingAccount == null || !requestAccount.equalsPendingAccount(pendingAccount)) {
                log.warn("Token found but credentials don't match.");
                return null;
            }

            if (requestAccount.getRegistrationData() == null || requestAccount.getPhoneNumber() == null) {
                log.warn("Account request is missing registrationData or phoneNumber");
                return null;
            }

            RegistrationData registrationData = registrationDataRepository.findFirstByDeviceInfo(requestAccount.getRegistrationData().getDeviceInfo());
            if (registrationData != null) {
                log.warn("Registration data is already used by another account. There will be two accounts bound to this device.");
                requestAccount.setRegistrationData(registrationData);
            } else {
                registrationData = requestAccount.getRegistrationData();
                //update sim card references to parent in order to retain id
                for (var simCard : registrationData.getMobileNumbersInfoList())
                    simCard.setRegistrationData(registrationData);
            }

            String jsonWebToken = jwtService.generateJwt(requestAccount);
            var account = accountsRepository.save(requestAccount);
            pendingAccountsRepository.delete(pendingAccount);

            if (account.getType() == UserType.SAILOR)
                createActiveSailorAccount(account);

            RegistrationData foundRegistrationData = registrationDataRepository.findFirstByDeviceInfo(registrationData.getDeviceInfo());
            if (foundRegistrationData != null)
                log.warn("Registration info deviceInfo duplicate in database! User has created new account using the same phone.");

            log.info("Registration successful. Returning JWT '" + jsonWebToken + "'.");
            return jsonWebToken;
        } catch (Exception e) {
            log.error("Exception occurred while registering.", e);
        }
        return null;
    }

    @Transactional
    public String login(Account account) {
        var foundAccountsList = accountsRepository.findAllByPassword(account.getPassword());
        if (foundAccountsList == null) {
            log.warn("No account was found with the given username/phone number and password.");
            return null;
        }

        for (var foundAccount : foundAccountsList) {
            if (foundAccount.getUsername().equals(account.getUsername()) || foundAccount.getPhoneNumber().equals(account.getPhoneNumber())) {
                log.info("Credentials matched. Found account.");
                return jwtService.generateJwt(account);
            } else
                log.warn("An account with given password found but neither username or phone number match.");
        }
        log.warn("Invalid credentials. Login failed");
        return null;
    }
}
