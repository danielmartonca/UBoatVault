package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.account.pending.PendingToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingTokenRepository extends JpaRepository<PendingToken, Long> {
    PendingToken findFirstByTokenValue(String token);

    PendingToken findFirstByAccount_UsernameAndAccount_Password(String username, String password);
}
