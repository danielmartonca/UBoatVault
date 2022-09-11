package com.uboat.vault.api.services;

import com.uboat.vault.api.model.enums.UserType;
import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.model.persistence.account.info.PhoneNumber;
import com.uboat.vault.api.model.persistence.account.info.RegistrationData;
import com.uboat.vault.api.model.persistence.account.info.SimCard;
import com.uboat.vault.api.model.persistence.sailing.sailor.ActiveSailor;
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
    private final ActiveSailorsRepository activeSailorsRepository;
    private final PhoneNumbersRepository phoneNumbersRepository;

    @Autowired
    public EntityService(RegistrationDataRepository registrationDataRepository, AccountsRepository accountsRepository, SimCardRepository simCardRepository, ActiveSailorsRepository activeSailorsRepository, PhoneNumbersRepository phoneNumbersRepository) {
        this.registrationDataRepository = registrationDataRepository;
        this.accountsRepository = accountsRepository;
        this.simCardRepository = simCardRepository;
        this.activeSailorsRepository = activeSailorsRepository;
        this.phoneNumbersRepository = phoneNumbersRepository;
    }

    /**
     * This method searches for an account in the database that matches the username OR phone number AND the password given as parameter
     *
     * @return the account if found,null otherwise
     */
    public Account findAccountByCredentials(Account requestAccount) {
        Account foundAccount;
        foundAccount = accountsRepository.findFirstByUsernameAndPassword(requestAccount.getUsername(), requestAccount.getPassword());
        if (foundAccount == null) {
            log.warn("Couldn't find account by username and password. Searching by phone number and password.");
            foundAccount = accountsRepository.findFirstByPhoneNumber_PhoneNumberAndPassword(requestAccount.getPhoneNumber().getPhoneNumber(), requestAccount.getPassword());
            if (foundAccount == null) {
                log.warn("Couldn't find account by phone number and password.");
                return null;
            }
            log.info("Found account by phone number and password.");
        }
        log.info("Credentials are ok. Account retrieved from database.");
        return foundAccount;
    }

    /**
     * Calls the other method with the same name but build the object first.
     */
    public Account findAccountByCredentials(String phoneNumber, String username, String password) {
        return findAccountByCredentials(Account.builder().username(username).password(password).phoneNumber(PhoneNumber.builder().phoneNumber(phoneNumber).build()).build());
    }

    /**
     * This method searches for the active sailor entity based on the data given as parameter
     */
    public ActiveSailor findActiveSailorByCredentials(Account requestAccount) {
        var foundAccount = findAccountByCredentials(requestAccount);
        if (foundAccount == null) {
            log.info("Request account or token are invalid.");
            return null;
        }

        if (foundAccount.getType() != UserType.SAILOR) {
            log.warn("Account found is not matching a sailor account. Aborting.");
            return null;
        }

        var foundSailorAccount = activeSailorsRepository.findFirstByAccountId(foundAccount.getId());
        if (foundSailorAccount == null) {
            log.warn("Sailor account is null. User hasn't setup any account details.");
            return null;
        }
        log.info("Active sailor account found.");
        return foundSailorAccount;
    }

    public ActiveSailor findActiveSailorBySailorId(String sailorId) {
        long sailorIdLong;
        try {
            sailorIdLong = Long.parseLong(sailorId);
        } catch (Exception e) {
            log.error("Exception while parsing sailorId " + sailorId);
            return null;
        }

        var foundActiveSailor = activeSailorsRepository.findFirstByAccountId(sailorIdLong);
        if (foundActiveSailor == null) {
            log.warn("Couldn't find active sailor account by id " + sailorIdLong);
            return null;
        }

        log.info("Found active sailor account.");
        return foundActiveSailor;
    }

    public Account findSailorAccountById(String accountId) {
        long sailorIdLong;
        try {
            sailorIdLong = Long.parseLong(accountId);
        } catch (Exception e) {
            log.error("Exception occurred while transforming sailorId String to Long", e);
            return null;
        }
        var foundAccountOptional = accountsRepository.findById(sailorIdLong);

        if (foundAccountOptional.isPresent()) {
            var foundAccount = foundAccountOptional.get();
            if (foundAccount.getType() == UserType.CLIENT) {
                log.warn("Account was found by id " + accountId + " but the account is matching a client account, not a sailor.");
                return null;
            }
            return foundAccountOptional.get();
        }

        log.warn("No account was found by id " + accountId);
        return null;
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
    public boolean checkDeviceRegistration(RegistrationData registrationData) {
        var foundRegistrationData = registrationDataRepository.findFirstByDeviceInfo(registrationData.getDeviceInfo());
        if (foundRegistrationData != null) {
            log.info("Found registration data by device info.");
            if (foundRegistrationData.getAccounts().size() == 1)
                log.info("There is only one account bound to this registration data.");
            else
                log.info("There are more than one account bound to this registration data.");
            return true;
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
                return true;
            }
        }
        log.info("Couldn't find any device by the given registration data.");
        return false;
    }
}
