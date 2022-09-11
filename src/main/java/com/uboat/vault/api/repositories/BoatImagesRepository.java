package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.sailing.sailor.BoatImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoatImagesRepository extends JpaRepository<BoatImage, Long> {
    BoatImage findByBoat_Id(Long boatId);
}
