package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.info.RegistrationData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationDataRepository extends JpaRepository<RegistrationData, Long> {
    RegistrationData findFirstByDeviceInfo(String value);
}
