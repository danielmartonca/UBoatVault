package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.sailing.sailor.BoatImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoatImagesRepository extends JpaRepository<BoatImage, Long> {
    BoatImage findByBoat_Id(Long boatId);
}
