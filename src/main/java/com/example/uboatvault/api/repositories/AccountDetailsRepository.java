package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.AccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDetailsRepository extends JpaRepository<AccountDetails, Long> {
}
