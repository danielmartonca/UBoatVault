package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.pending.PendingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingAccountsRepository extends JpaRepository<PendingAccount, Long> {
    PendingAccount findFirstByUsername(String username);

    PendingAccount findFirstByPhoneNumberAndPhoneDialCodeAndPhoneIsoCode(String phoneNumber,String dialCode,String isoCode);
    PendingAccount findFirstByToken(String pendingTokenValue);
}
