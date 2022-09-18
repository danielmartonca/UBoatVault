package com.uboat.vault.api.services;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.http.RequestRegistrationData;
import com.uboat.vault.api.model.other.Credentials;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.account.info.PhoneNumber;
import com.uboat.vault.api.model.persistence.account.info.SimCard;
import com.uboat.vault.api.model.persistence.sailing.sailor.Sailor;
import com.uboat.vault.api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntityService {
    private final Logger log = LoggerFactory.getLogger(AccountsService.class);

    private final AccountsRepository accountsRepository;
    private final RegistrationDataRepository registrationDataRepository;
    private final SimCardRepository simCardRepository;
    private final SailorsRepository sailorsRepository;
    private final PhoneNumbersRepository phoneNumbersRepository;

    @Autowired
    public EntityService(RegistrationDataRepository registrationDataRepository, AccountsRepository accountsRepository, SimCardRepository simCardRepository, SailorsRepository sailorsRepository, PhoneNumbersRepository phoneNumbersRepository) {
        this.registrationDataRepository = registrationDataRepository;
        this.accountsRepository = accountsRepository;
        this.simCardRepository = simCardRepository;
        this.sailorsRepository = sailorsRepository;
        this.phoneNumbersRepository = phoneNumbersRepository;
    }

    public Account findAccountByUsername(String username) {
        Account foundAccount;
        if ((username == null || username.isBlank()))
            throw new RuntimeException("Both phone number and username are null/empty while retrieving account by credentials.");

        foundAccount = accountsRepository.findFirstByUsername(username);
        log.debug("Account found by username.");
        return foundAccount;
    }

    public Account findAccountByCredentials(Credentials credentials) {
        return findAccountByCredentials(credentials.getPhoneNumber(), credentials.getUsername(), credentials.getPassword());
    }

    /**
     * This method searches for an account in the database that matches the username OR phone number AND the password given as parameter
     *
     * @return the account if found,null otherwise
     */
    public Account findAccountByCredentials(String phoneNumber, String username, String password) {
        Account foundAccount = null;
        if ((username == null || username.isBlank()) && (phoneNumber == null || phoneNumber.isBlank()))
            throw new RuntimeException("Both phone number and username are null/empty while retrieving account by credentials.");

        if (username != null && !username.isBlank())
            foundAccount = accountsRepository.findFirstByUsernameAndPassword(username, password);

        if (foundAccount == null) {
            log.warn("Couldn't find account by username and password. Searching by phone number and password.");
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                foundAccount = accountsRepository.findFirstByPhoneNumber_PhoneNumberAndPassword(phoneNumber, password);
                if (foundAccount == null) {
                    log.warn("Couldn't find account by phone number and password.");
                    return null;
                }
                log.info("Found account by phone number and password.");
            }

        }

        log.info("Credentials are ok. Account retrieved from database.");
        return foundAccount;
    }

    /**
     * This method searches for the active sailor entity based on the data given as parameter
     */
    public Sailor findSailorByCredentials(Account account) {
        var foundAccount = findAccountByCredentials(Credentials.fromAccount(account));
        if (foundAccount == null) {
            log.info("Request account or token are invalid.");
            return null;
        }

        if (foundAccount.getType() != UserType.SAILOR) {
            log.warn("Account found but is not matching a sailor account.");
            return null;
        }

        var foundSailorAccount = sailorsRepository.findFirstByAccountId(foundAccount.getId());
        if (foundSailorAccount == null) {
            log.warn("Sailor account is null. User hasn't setup any account details.");
            return null;
        }

        log.info("Active sailor account found.");
        return foundSailorAccount;
    }

    public Sailor findSailorBySailorId(String sailorId) {
        long sailorIdLong;
        try {
            sailorIdLong = Long.parseLong(sailorId);
        } catch (Exception e) {
            log.error("Exception while parsing sailorId " + sailorId);
            return null;
        }

        var sailorOptional = sailorsRepository.findById(sailorIdLong);
        if (sailorOptional.isEmpty()) {
            log.warn("Couldn't find sailor account by id " + sailorIdLong);
            return null;

        }

        log.info("Found sailor account.");
        return sailorOptional.get();
    }

    public Sailor findSailorByAccountId(String accountId) {
        long accountIdLong;
        try {
            accountIdLong = Long.parseLong(accountId);
        } catch (Exception e) {
            log.error("Exception occurred while transforming sailorId String to Long", e);
            return null;
        }

        var sailor = sailorsRepository.findFirstByAccountId(accountIdLong);
        if (sailor == null) {
            log.warn("No account was found by id " + accountId);
            return null;
        }
        log.warn("Found sailor account by id.");

        return sailor;
    }

    public Sailor findSailorByJwt(JwtService.Data jwtData) {
        var account = findAccountByUsername(jwtData.username());
        //can't be null due to API being accessible only by sailors
        return sailorsRepository.findFirstByAccountId(account.getId());
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
     * This method searches if any part of the registrationData exists in the database and returns true if any are found
     */
    @Transactional
    public UBoatStatus checkDeviceRegistration(RequestRegistrationData registrationData) {
        var foundRegistrationData = registrationDataRepository.findFirstByDeviceInfo(registrationData.getDeviceInfo());
        if (foundRegistrationData != null) {
            log.info("Found registration data by device info.");
            if (foundRegistrationData.getAccounts().size() == 1)
                log.info("There is only one account bound to this registration data.");
            else
                log.info("There are more than one account bound to this registration data.");
            return UBoatStatus.DEVICE_INFO_ALREADY_USED;
        }

        for (var simCard : registrationData.getMobileNumbersInfoList()) {
            SimCard extractedSimCard = simCardRepository.findFirstByNumberAndDisplayNameAndCountryIso(simCard.getNumber(), simCard.getDisplayName(), simCard.getCountryIso());
            if (extractedSimCard != null) {
                log.info("Found registration data by simCard.");
                foundRegistrationData = extractedSimCard.getRegistrationData();
                if (foundRegistrationData.getAccounts().size() == 1)
                    log.info("There is only one account bound to this registration data.");
                else
                    log.info("There are more than one account bound to this registration data.");
                return UBoatStatus.SIM_ALREADY_USED;
            }
        }
        log.info("Couldn't find any device by the given registration data.");
        return UBoatStatus.DEVICE_NOT_REGISTERED;
    }
}
