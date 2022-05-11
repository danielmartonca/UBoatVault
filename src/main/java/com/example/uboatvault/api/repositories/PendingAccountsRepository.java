package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.PendingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingAccountsRepository extends JpaRepository<PendingAccount, Long> {
    PendingAccount findFirstByUsernameAndPassword(String username, String password);
}
