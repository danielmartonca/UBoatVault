package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.account.PendingToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingTokenRepository extends JpaRepository<PendingToken, Long> {
    PendingToken findFirstByTokenValue(String token);

    PendingToken findFirstByAccount_UsernameAndAccount_Password(String username, String password);
}
