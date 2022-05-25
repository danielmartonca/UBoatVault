package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository extends JpaRepository<Account, Long> {
    Account findFirstByUsername(String username);
    Account findFirstByUsernameAndPassword(String username, String password);
    Account findFirstByPassword(String password);
}
