package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.sailing.LocationData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationDataRepository extends JpaRepository<LocationData,Long> {
}
