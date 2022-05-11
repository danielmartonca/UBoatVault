package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.persistence.PendingToken;
import com.example.uboatvault.api.model.persistence.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository extends JpaRepository<Account, Long> {
    Account findFirstByUsername(String username);
}
