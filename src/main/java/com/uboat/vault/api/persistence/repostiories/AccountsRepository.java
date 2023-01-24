package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository extends JpaRepository<Account, Long> {
    Account findFirstByAccountDetails_Email(String email);

    Account findFirstByUsername(String username);

    Account findFirstByPhoneNumber(String phoneNumber);

    Account findFirstByPhoneNumberAndPhoneDialCodeAndPhoneIsoCode(String phoneNumber, String dialCode, String isoCode);
}
