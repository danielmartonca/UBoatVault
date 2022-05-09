package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.SimCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimCardRepository extends JpaRepository<SimCard, Long> {
    SimCard findFirstByNumberAndDisplayNameAndCountryIso(String number, String displayName, String countryIso);
}
