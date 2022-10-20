package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.sailing.sailor.Boat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoatsRepository extends JpaRepository<Boat, Long> {
}