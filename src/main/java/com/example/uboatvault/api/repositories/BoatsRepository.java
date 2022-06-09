package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.sailing.sailor.Boat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoatsRepository extends JpaRepository<Boat, Long> {
}