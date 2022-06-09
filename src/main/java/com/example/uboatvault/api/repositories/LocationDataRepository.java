package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.sailing.LocationData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationDataRepository extends JpaRepository<LocationData,Long> {
}
