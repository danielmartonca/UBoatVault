package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.sailing.sailor.BoatImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoatImagesRepository extends JpaRepository<BoatImage, Long> {
    BoatImage findBoatImageByHash(String hash);
}
