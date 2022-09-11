package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.account.info.AccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDetailsRepository extends JpaRepository<AccountDetails, Long> {
}
