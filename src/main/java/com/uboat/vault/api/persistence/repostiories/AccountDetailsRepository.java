package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.info.AccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDetailsRepository extends JpaRepository<AccountDetails, Long> {
}
