package com.uboat.vault.api.business.services;

import com.uboat.vault.api.model.domain.account.account.Account;
import com.uboat.vault.api.model.enums.UserType;
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
    public UserDetails loadUserByUsername(String jwtDataAsTabSeparatedString) throws UsernameNotFoundException {
        var split = jwtDataAsTabSeparatedString.split("\t");
        final var userType = UserType.valueOf(split[0]).getType();
        final String username = split[1];
        final String phoneNumber = split[2];

        Account account = null;
        if (!phoneNumber.equals("null"))
            account = accountsRepository.findFirstByPhoneNumber(phoneNumber);

        if (account == null && !username.equals("null"))
            account = accountsRepository.findFirstByUsername(username);

        if (account == null)
            throw new UsernameNotFoundException("User not found.");

        return new User(account.getUsername(), account.getPassword(), List.of(new SimpleGrantedAuthority(userType)));
    }
}