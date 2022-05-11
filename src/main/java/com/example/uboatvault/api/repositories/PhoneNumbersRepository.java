package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneNumbersRepository extends JpaRepository<PhoneNumber, Long> {
    PhoneNumber findFirstByPhoneNumberAndDialCodeAndIsoCode(String phoneNumber, String dialCode, String isoCode);
}