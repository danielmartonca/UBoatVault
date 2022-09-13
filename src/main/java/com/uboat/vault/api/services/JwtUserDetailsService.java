package com.uboat.vault.api.services;

import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.repositories.AccountsRepository;
import com.uboat.vault.api.utilities.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

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
        final String username = split[0];
        final String phoneNumber = split[1];

        Account account = null;

        if (!phoneNumber.equals("null"))
            account = findAccountByPhoneNumber(phoneNumber);

        if (account == null && !username.equals("null"))
            account = findAccountByUsername(username);

        if (account == null)
            throw new UsernameNotFoundException("User not found.");

        var accountType = account.getType().getType();
        var authorities = List.of(new SimpleGrantedAuthority(accountType));

        return new User(account.getUsername(), account.getPassword(), authorities);
    }
}