package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.account.pending.PendingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingAccountsRepository extends JpaRepository<PendingAccount, Long> {
    PendingAccount findFirstByUsernameAndPassword(String username, String password);
}
