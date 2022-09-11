package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.account.info.SimCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimCardRepository extends JpaRepository<SimCard, Long> {
    SimCard findFirstByNumberAndDisplayNameAndCountryIso(String number, String displayName, String countryIso);
}
