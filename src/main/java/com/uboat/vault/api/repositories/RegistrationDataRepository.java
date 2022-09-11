package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.account.info.RegistrationData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationDataRepository extends JpaRepository<RegistrationData, Long> {
    RegistrationData findFirstByDeviceInfo(String value);
}
