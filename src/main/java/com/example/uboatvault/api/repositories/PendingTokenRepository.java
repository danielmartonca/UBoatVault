package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.PendingToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingTokenRepository extends JpaRepository<PendingToken, Long> {
    PendingToken findFirstByTokenValue(String token);
    PendingToken deleteByTokenValue(String value);
}
