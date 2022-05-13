package com.example.uboatvault.api.services;

import com.example.uboatvault.api.controllers.RegistrationController;
import com.example.uboatvault.api.model.persistence.*;
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

    private final Pattern phoneNumberPattern = Pattern.compile("^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$");
    private final Pattern usernamePattern = Pattern.compile("^[a-zA-z][a-zA-z0-9]*$");
    private final Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    @Autowired
    public RegistrationService(TokenService tokenService, RegistrationDataRepository registrationDataRepository, SimCardRepository simCardRepository, PendingTokenRepository pendingTokenRepository, PendingAccountsRepository pendingAccountsRepository, AccountsRepository accountsRepository, PhoneNumbersRepository phoneNumbersRepository, TokensRepository tokensRepository) {
        this.tokenService = tokenService;
        this.registrationDataRepository = registrationDataRepository;
        this.simCardRepository = simCardRepository;
        this.pendingTokenRepository = pendingTokenRepository;
        this.pendingAccountsRepository = pendingAccountsRepository;
        this.accountsRepository = accountsRepository;
        this.phoneNumbersRepository = phoneNumbersRepository;
        this.tokensRepository = tokensRepository;
    }


    private boolean isAccountAlreadyExisting(Account account) {
        try {
            Account foundAccount = accountsRepository.findFirstByUsernameAndPassword(account.getUsername(), account.getPassword());
            if (foundAccount != null) return true;
            PhoneNumber phoneNumber = account.getPhoneNumber();
            PhoneNumber foundPhoneNumber = phoneNumbersRepository.findFirstByPhoneNumberAndDialCodeAndIsoCode(phoneNumber.getPhoneNumber(), phoneNumber.getDialCode(), phoneNumber.getIsoCode());
            return foundPhoneNumber != null;
        } catch (Exception e) {
            return false;
        }
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

    /**
     * This method searches if any part of the registrationData exists in the database and returns its token if found (or generates a new one if it's older than 30 minutes)
     */
    @Transactional
    public String searchForTokenBasedOnRegistrationData(RegistrationData registrationData) {
        var foundRegistrationData = registrationDataRepository.findFirstByDeviceInfo(registrationData.getDeviceInfo());
        if (foundRegistrationData != null) {
            if (tokenService.isTokenDeprecated(foundRegistrationData.getAccount())) {
                log.info("Token is deprecated.");
                tokenService.updateToken(foundRegistrationData.getAccount());
                registrationDataRepository.save(foundRegistrationData);
            }
            log.info("Found token by registration data.");
            return foundRegistrationData.getAccount().getToken().getTokenValue();
        }

        for (var simCard : registrationData.getMobileNumbersInfoList()) {
            SimCard extractedSimCard = simCardRepository.findFirstByNumberAndDisplayNameAndCountryIso(simCard.getNumber(), simCard.getDisplayName(), simCard.getCountryIso());
            if (extractedSimCard != null) {
                Account account = extractedSimCard.getRegistrationData().getAccount();
                if (tokenService.isTokenDeprecated(account)) {
                    log.info("Token is deprecated.");
                    tokenService.updateToken(account);
                    registrationDataRepository.save(registrationData);
                }
                log.info("Found token by simCard.");
                return account.getToken().getTokenValue();
            }
        }
        log.info("Couldn't find any token.");
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
                Account account = foundRegistrationData.getAccount();
                if (tokenService.isTokenDeprecated(account)) {
                    tokenService.updateToken(account);
                    registrationDataRepository.save(foundRegistrationData);
                    log.info("Found token.");
                    return account.getToken().getTokenValue();
                }
            }
        }
        log.info("Couldn't find any token.");
        return null;
    }

    /**
     * This method returns a new token if registrationData is not found in database, or its token if it is found.
     */
    public String requestRegistrationToken(Account account) {
        try {
            if (isAccountAlreadyExisting(account)) {
                log.warn("Account already exists.");
                return null;
            }

            if (isPendingAccountAlreadyExisting(account)) {
                log.warn("Pending account already exists returning the token.");
                return getPendingAccountToken(account);
            }

            String token = tokenService.generateToken();
            log.info("A new token will be generated for this account.");

            //creating new pendingAccount and it's references
            var newPendingAccount = new PendingAccount(account);
            PendingToken pendingToken = new PendingToken(token, newPendingAccount);
            newPendingAccount.setPendingToken(pendingToken);

            pendingAccountsRepository.save(newPendingAccount);
            pendingTokenRepository.save(pendingToken);
            log.info("Created registration request. Returning token '" + token + "'.");
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

    public boolean passwordMatchesPattern(String password) {
        Matcher m = passwordPattern.matcher(password);
        return m.matches();
    }

    /**
     * Checks if an account exists with the given username in the database.
     */
    public boolean isUsernameUsed(String username) {
        boolean usernameExists = true;
        Account account = accountsRepository.findFirstByUsername(username);
        if (account == null) usernameExists = false;
        log.info(usernameExists ? "Username is already used." : "Username is not used.");
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
    public String register(Account account, String token) {
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

            PendingAccount pendingAccount = pendingAccountsRepository.findFirstByUsernameAndPassword(account.getUsername(), account.getPassword());
            if (pendingAccount == null || !account.equalsPendingAccount(pendingAccount)) {
                log.warn("Token found but credentials don't match.");
                return null;
            }

            if (account.getRegistrationData() == null ||
                    account.getPhoneNumber() == null) {
                log.warn("Account request is missing registrationData or phoneNumber");
                return null;
            }
            //update sim card references to parent in order to retain id
            for (var simCard : account.getRegistrationData().getMobileNumbersInfoList())
                simCard.setRegistrationData(account.getRegistrationData());

            tokenService.updateToken(account);

            pendingToken = pendingTokenRepository.findFirstByTokenValue(token);
            pendingTokenRepository.delete(pendingToken);
            log.info("Registration successful. Returning token '" + token + "'.");
            return account.getToken().getTokenValue();
        } catch (Exception e) {
            log.error("Exception occurred while registering.", e);
        }
        return null;
    }
}
