package com.uboat.vault.api.business.services;

import com.uboat.vault.api.model.domain.account.Account;
import com.uboat.vault.api.persistence.repostiories.AccountsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {
    private final AccountsRepository accountsRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameAndPhoneNumber) throws UsernameNotFoundException {
        var split = usernameAndPhoneNumber.split("\t");
        final String username = split[0];
        final String phoneNumber = split[1];

        Account account = null;

        if (!phoneNumber.equals("null"))
            account = accountsRepository.findFirstByPhoneNumber_PhoneNumber(phoneNumber);

        if (account == null && !username.equals("null"))
            account = accountsRepository.findFirstByUsername(username);

        if (account == null)
            throw new UsernameNotFoundException("User not found.");

        var accountType = account.getType().getType();
        var authorities = List.of(new SimpleGrantedAuthority(accountType));

        return new User(account.getUsername(), account.getPassword(), authorities);
    }
}