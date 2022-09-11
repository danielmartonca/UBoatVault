package com.uboat.vault.api.services;

import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.repositories.AccountsRepository;
import com.uboat.vault.api.utilities.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final Logger log = LoggerFactory.getLogger(AccountsService.class);
    private final AccountsRepository accountsRepository;

    @Autowired
    public JwtUserDetailsService(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    public Account findAccountByUsername(String username) {
        var foundAccount = accountsRepository.findFirstByUsername(username);
        if (foundAccount == null)
            log.warn(LoggingUtils.colorString("Couldn't find any account by username '" + username + "'.", LoggingUtils.TextColor.RED));
        else
            log.info(LoggingUtils.colorString("[Filter] Found account by username '" + username + "'.", LoggingUtils.TextColor.GREEN));
        return foundAccount;
    }

    public Account findAccountByPhoneNumber(String phoneNumber) {
        var foundAccount = accountsRepository.findFirstByPhoneNumber_PhoneNumber(phoneNumber);
        if (foundAccount == null)
            log.warn(LoggingUtils.colorString("Couldn't find any account by phone number '" + phoneNumber + "'.", LoggingUtils.TextColor.RED));
        else
            log.info(LoggingUtils.colorString("[Filter] Found account by phone number '" + phoneNumber + "'.", LoggingUtils.TextColor.GREEN));
        return foundAccount;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameAndPhoneNumber) throws UsernameNotFoundException {
        var split = usernameAndPhoneNumber.split("\t");
        final String phoneNumber = split[0];
        final String username = split[1];

        Account foundAccount = null;

        if (!phoneNumber.equals("null"))
            foundAccount = findAccountByPhoneNumber(phoneNumber);

        if (foundAccount == null && !username.equals("null"))
            foundAccount = findAccountByUsername(username);

        if (foundAccount != null) {
            return new User(foundAccount.getUsername(), foundAccount.getPassword(), new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User not found with credentials: '" + usernameAndPhoneNumber.replace("null", "").replace("\t", "") + "'");
        }
    }
}