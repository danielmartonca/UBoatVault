package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.sailing.LocationData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationDataRepository extends JpaRepository<LocationData,Long> {
}
