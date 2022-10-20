package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.info.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneNumbersRepository extends JpaRepository<PhoneNumber, Long> {
    PhoneNumber findFirstByPhoneNumberAndDialCodeAndIsoCode(String phoneNumber, String dialCode, String isoCode);
}