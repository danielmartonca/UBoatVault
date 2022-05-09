package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.RegistrationData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationDataRepository extends JpaRepository<RegistrationData, Long> {
    RegistrationData findFirstByToken(String token);

    RegistrationData findFirstByDeviceInfo(String value);
}
