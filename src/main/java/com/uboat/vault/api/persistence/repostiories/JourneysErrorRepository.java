package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.sailing.JourneyError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JourneysErrorRepository extends JpaRepository<JourneyError, Long> {
}
