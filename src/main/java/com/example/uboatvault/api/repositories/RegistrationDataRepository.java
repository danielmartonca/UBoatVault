package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.account.RegistrationData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationDataRepository extends JpaRepository<RegistrationData, Long> {
    RegistrationData findFirstByDeviceInfo(String value);
}
