package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokensRepository extends JpaRepository<Token, Long> {
    Token findFirstByTokenValue(String token);
}