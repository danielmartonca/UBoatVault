package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountsRepository extends JpaRepository<Account, Long> {
    Account findFirstByAccountDetails_Email(String email);

    Account findFirstByUsername(String username);

    Account findFirstByPhoneNumber(String phoneNumber);

    Account findFirstByPhoneNumberAndPhoneDialCodeAndPhoneIsoCode(String phoneNumber, String dialCode, String isoCode);

    Account findFirstByUsernameAndPassword(String username, String password);

    Account findFirstByPhoneNumberAndPassword(String phoneNumber, String password);

    List<Account> findAllByPassword(String password);
}
