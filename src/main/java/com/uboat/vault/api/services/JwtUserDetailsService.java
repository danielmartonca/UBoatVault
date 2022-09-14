package com.uboat.vault.api.services;

import com.uboat.vault.api.model.persistence.account.Account;
import com.uboat.vault.api.repositories.AccountsRepository;
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
    private final AccountsRepository accountsRepository;

    @Autowired
    public JwtUserDetailsService(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    private Account findAccountByUsername(String username) {
        return accountsRepository.findFirstByUsername(username);
    }

    private Account findAccountByPhoneNumber(String phoneNumber) {
        return accountsRepository.findFirstByPhoneNumber_PhoneNumber(phoneNumber);
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