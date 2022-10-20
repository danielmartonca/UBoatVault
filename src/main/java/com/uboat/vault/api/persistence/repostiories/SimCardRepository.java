package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.info.SimCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimCardRepository extends JpaRepository<SimCard, Long> {
    SimCard findFirstByNumberAndDisplayNameAndCountryIso(String number, String displayName, String countryIso);
}
