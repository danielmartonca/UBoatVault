package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.sailing.sailor.Boat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoatsRepository extends JpaRepository<Boat, Long> {
}