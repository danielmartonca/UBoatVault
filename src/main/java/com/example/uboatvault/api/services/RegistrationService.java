package com.example.uboatvault.api.services;

import com.example.uboatvault.api.controllers.RegistrationController;
import com.example.uboatvault.api.model.enums.UserType;
import com.example.uboatvault.api.model.persistence.account.*;
import com.example.uboatvault.api.model.persistence.account.info.PhoneNumber;
import com.example.uboatvault.api.model.persistence.account.info.RegistrationData;
import com.example.uboatvault.api.model.persistence.account.info.SimCard;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegistrationService {
    private final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    private final TokenService tokenService;

    private final RegistrationDataRepository registrationDataRepository;
    private final SimCardRepository simCardRepository;
    private final PendingTokenRepository pendingTokenRepository;
    private final PendingAccountsRepository pendingAccountsRepository;
    private final AccountsRepository accountsRepository;
    private final PhoneNumbersRepository phoneNumbersRepository;
    private final TokensRepository tokensRepository;
    private final ActiveSailorsRepository activeSailorsRepository;

    private final Pattern phoneNumberPattern = Pattern.compile("^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$");
    private final Pattern usernamePattern = Pattern.compile("^[a-zA-z][a-zA-z0-9]*$");
//    private final Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    @Autowired
    public RegistrationService(TokenService tokenService, RegistrationDataRepository registrationDataRepository, SimCardRepository simCardRepository, PendingTokenRepository pendingTokenRepository, PendingAccountsRepository pendingAccountsRepository, AccountsRepository accountsRepository, PhoneNumbersRepository phoneNumbersRepository, TokensRepository tokensRepository, ActiveSailorsRepository activeSailorsRepository) {
        this.tokenService = tokenService;
        this.registrationDataRepository = registrationDataRepository;
        this.simCardRepository = simCardRepository;
        this.pendingTokenRepository = pendingTokenRepository;
        this.pendingAccountsRepository = pendingAccountsRepository;
        this.accountsRepository = accountsRepository;
        this.phoneNumbersRepository = phoneNumbersRepository;
        this.tokensRepository = tokensRepository;
        this.activeSailorsRepository = activeSailorsRepository;
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

    private void logIfRegistrationDataIsAlreadyInDatabase(RegistrationData registrationData) {
        RegistrationData foundRegistrationData = registrationDataRepository.findFirstByDeviceInfo(registrationData.getDeviceInfo());
        if (foundRegistrationData != null)
            log.warn("Registration info deviceInfo duplicate in database! User has created new account using the same phone.");
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

    /**
     * This method searches if any part of the registrationData exists in the database and returns its token if found (or generates a new one if it's deprecated)
     */
    @Transactional
    public String searchForTokenBasedOnRegistrationData(RegistrationData registrationData) {
        var foundRegistrationData = registrationDataRepository.findFirstByDeviceInfo(registrationData.getDeviceInfo());
        if (foundRegistrationData != null) {
            log.info("Found registration data by device info.");
            if (foundRegistrationData.getAccounts().size() == 1) {
                log.info("There is only one account bound to this registration data. Returning it's token: " + foundRegistrationData.getAccounts().get(0).getToken().getTokenValue());
                return foundRegistrationData.getAccounts().get(0).getToken().getTokenValue();
            }
            log.warn("There are more than one account bound to this registration data. Will not return any token.");
            return null;
        }

        for (var simCard : registrationData.getMobileNumbersInfoList()) {
            SimCard extractedSimCard = simCardRepository.findFirstByNumberAndDisplayNameAndCountryIso(simCard.getNumber(), simCard.getDisplayName(), simCard.getCountryIso());
            if (extractedSimCard != null) {
                log.info("Found registration data by simCard.");
                foundRegistrationData = extractedSimCard.getRegistrationData();
                if (foundRegistrationData.getAccounts().size() == 1) {
                    log.info("There is only one account bound to this registration data. Returning it's token: " + foundRegistrationData.getAccounts().get(0).getToken().getTokenValue());
                    return foundRegistrationData.getAccounts().get(0).getToken().getTokenValue();
                }
                log.warn("There are more than one account bound to this registration data. Will not return any token.");
                return null;
            }
        }
        log.info("Couldn't find any token by the given registration data.");
        return null;
    }

    /**
     * This method check if the token passed as parameter corresponds to registrationData in the database
     * If they match and token is older than 30 minutes, a new token will be generated, and it will update the database.
     */
    @Transactional
    public String searchForTokenByValue(String tokenValue, RegistrationData registrationData) {
        Token token = tokensRepository.findFirstByTokenValue(tokenValue);
        if (token != null) {
            RegistrationData foundRegistrationData = token.getAccount().getRegistrationData();
            if (!foundRegistrationData.equals(registrationData)) {
                log.info("Found token.");
                return token.getTokenValue();
            }
        }
        log.info("Couldn't find any token.");
        return null;
    }

    /**
     * This method returns a new token if registrationData is not found in database, or its token if it is found.
     */
    @Transactional
    public String requestRegistrationToken(Account account) {
        try {
            if (isAccountAlreadyExisting(account)) {
                log.warn("Account already exists with the given credentials.");
                return null;
            }

            if (isPendingAccountAlreadyExisting(account)) {
                log.warn("Pending account already exists returning the token.");
                return getPendingAccountToken(account);
            }

            log.info("New pendingAccount and pendingToken will be generated.");

            //creating new pendingAccount and it's pendingToken
            String token = tokenService.generateTokenString();
            var newPendingAccount = new PendingAccount(account);
            PendingToken newPendingToken = new PendingToken(token);
            newPendingAccount.setPendingToken(newPendingToken);
            newPendingToken.setAccount(newPendingAccount);

            pendingAccountsRepository.save(newPendingAccount);
            log.info("Created new pending registration account and token. Returning token '" + token + "'.");
            return token;
        } catch (Exception e) {
            log.error("Error while requesting new registration.", e);
            return null;
        }
    }

    public boolean usernameMatchesPattern(String username) {
        Matcher m = usernamePattern.matcher(username);
        return m.matches();
    }

    public boolean phoneNumberMatchesPattern(String phoneNumber) {
        Matcher m = phoneNumberPattern.matcher(phoneNumber);
        return m.matches();
    }

    /**
     * Checks if an account exists with the given username in the database.
     */
    public boolean isUsernameUsed(String username) {
        boolean usernameExists = true;
        Account account = accountsRepository.findFirstByUsername(username);
        if (account == null) usernameExists = false;
        log.info(usernameExists ? "Username found in the database." : "Username not found in the database.");
        return usernameExists;
    }

    /**
     * Checks if an account exists with the given phone number in the database.
     */
    public boolean isPhoneNumberUsed(String phoneNumber, String dialCode, String isoCode) {
        boolean phoneNumberAlreadyUsed = true;
        PhoneNumber phoneNumberFound = phoneNumbersRepository.findFirstByPhoneNumberAndDialCodeAndIsoCode(phoneNumber, dialCode, isoCode);
        if (phoneNumberFound == null) phoneNumberAlreadyUsed = false;
        else if (phoneNumberFound.getAccount() == null) phoneNumberAlreadyUsed = false;
        log.info(phoneNumberAlreadyUsed ? "Phone number is already used." : "Phone number is not used.");
        return phoneNumberAlreadyUsed;
    }

    /**
     * Saves a new device in the database based on registrationData and returns its token if successful.
     */
    @Transactional
    public String register(Account requestAccount, String token) {
        try {
            PendingToken pendingToken = pendingTokenRepository.findFirstByTokenValue(token);
            if (pendingToken == null) {
                log.warn("There is no matching pending token to the provided token.");
                return null;
            }

            if (!token.equals(pendingToken.getTokenValue())) {
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

            String tokenString = tokenService.generateTokenString();
            Token accountToken = new Token(tokenString);
            accountToken.setAccount(requestAccount);
            requestAccount.setToken(accountToken);

            var account = accountsRepository.save(requestAccount);
            pendingAccountsRepository.delete(pendingAccount);

            if (account.getType() == UserType.SAILOR)
                createActiveSailorAccount(account);

            logIfRegistrationDataIsAlreadyInDatabase(account.getRegistrationData());

            log.info("Registration successful. Returning token '" + token + "'.");
            return account.getToken().getTokenValue();
        } catch (Exception e) {
            log.error("Exception occurred while registering.", e);
        }
        return null;
    }
}
