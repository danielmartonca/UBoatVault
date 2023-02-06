package com.uboat.vault.api.business.services;

import com.uboat.vault.api.business.services.security.CryptoService;
import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.domain.account.account.SimCard;
import com.uboat.vault.api.model.domain.account.sailor.Sailor;
import com.uboat.vault.api.model.dto.RegistrationDataDTO;
import com.uboat.vault.api.model.dto.UBoatDTO;
import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.model.other.Credentials;
import com.uboat.vault.api.persistence.repostiories.AccountsRepository;
import com.uboat.vault.api.persistence.repostiories.RegistrationDataRepository;
import com.uboat.vault.api.persistence.repostiories.SailorsRepository;
import com.uboat.vault.api.persistence.repostiories.SimCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EntityService {
    private final CryptoService cryptoService;
    private final AccountsRepository accountsRepository;
    private final RegistrationDataRepository registrationDataRepository;
    private final SimCardRepository simCardRepository;
    private final SailorsRepository sailorsRepository;

    public Account findAccountByJwtData(JwtService.Data jwtData) {
        var account = findAccountByUsername(jwtData.username());
        if (account == null) account = findAccountByPhoneNumber(jwtData.phoneNumber());
        return account;
    }

    public Account findAccountByUsername(String username) {
        Account foundAccount;
        if (Strings.isEmpty(username))
            throw new RuntimeException("Username is null while trying to retrieve account.");

        foundAccount = accountsRepository.findFirstByUsername(username);
        log.debug("Account found by username.");
        return foundAccount;
    }

    public Account findAccountByPhoneNumber(String phoneNumber) {
        Account foundAccount;
        if (Strings.isEmpty(phoneNumber))
            throw new RuntimeException("Phone number is null/empty while trying to retrieve account.");

        foundAccount = accountsRepository.findFirstByPhoneNumber(phoneNumber);
        log.debug("Account found by phone number.");
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
        Account foundAccount;

        if (Strings.isEmpty(username) && Strings.isEmpty(phoneNumber))
            throw new RuntimeException("Both phone number and username are null/empty while retrieving account by credentials.");

        if (Strings.isNotEmpty(username)) {
            foundAccount = accountsRepository.findFirstByUsername(username);
            if (foundAccount != null) {
                if (cryptoService.matchesHash(password, foundAccount.getPassword())) {
                    log.info("Found {} account by username and password.", foundAccount.getType());
                    return foundAccount;
                }
            } else
                log.warn("Couldn't find account by username and password.");
        } else log.warn("Username is empty.");

        foundAccount = accountsRepository.findFirstByPhoneNumber(phoneNumber);
        if (foundAccount != null) {
            if (cryptoService.matchesHash(password, foundAccount.getPassword())) {
                log.info("Found {} account by phone number and password.", foundAccount.getType());
                return foundAccount;
            }
        } else log.warn("Couldn't find account by phone number.");


        log.info("Could not find account with the given credentials.");
        return null;
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

    public Sailor findSailorByJwt(JwtService.Data jwtData) {
        var account = findAccountByJwtData(jwtData);
        //can't be null due to API being accessible only by sailors
        return sailorsRepository.findFirstByAccountId(account.getId());
    }

    public boolean isEmailUsed(String email) {
        boolean emailExists = true;
        Account account = accountsRepository.findFirstByAccountDetails_Email(email);
        if (account == null) emailExists = false;
        log.info(emailExists ? "Email found in the database." : "Email not found in the database.");
        return emailExists;
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
        var account = accountsRepository.findFirstByPhoneNumberAndPhoneDialCodeAndPhoneIsoCode(phoneNumber, dialCode, isoCode);
        if (account == null) phoneNumberAlreadyUsed = false;
        log.info(phoneNumberAlreadyUsed ? "Phone number is already used." : "Phone number is not used.");
        return phoneNumberAlreadyUsed;
    }

    /**
     * This method searches if any part of the registrationData exists in the database and returns true if any are found
     */
    @Transactional
    public UBoatDTO checkDeviceRegistration(RegistrationDataDTO registrationData) {
        try {
            var foundRegistrationData = registrationDataRepository.findFirstByDeviceInfo(registrationData.getDeviceInfo());
            if (foundRegistrationData != null) {
                log.info("Found registration data by device info.");
                if (foundRegistrationData.getAccounts().size() == 1)
                    log.info("There is only one account bound to this registration data.");
                else
                    log.info("There are more than one account bound to this registration data.");
                return new UBoatDTO(UBoatStatus.DEVICE_INFO_ALREADY_USED, true);
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
                    return new UBoatDTO(UBoatStatus.SIM_ALREADY_USED, true);
                }
            }
            log.info("Couldn't find any device by the given registration data.");
            return new UBoatDTO(UBoatStatus.DEVICE_NOT_REGISTERED, false);
        } catch (Exception e) {
            log.error("An exception occurred during checkDeviceRegistration workflow.", e);
            return new UBoatDTO(UBoatStatus.VAULT_INTERNAL_SERVER_ERROR);
        }
    }
}
