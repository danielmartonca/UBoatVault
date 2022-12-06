package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.pending.PendingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingAccountsRepository extends JpaRepository<PendingAccount, Long> {
    PendingAccount findFirstByUsernameAndPassword(String username, String password);
    PendingAccount findFirstByToken(String pendingTokenValue);
}
