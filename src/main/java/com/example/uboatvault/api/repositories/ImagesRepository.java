package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ImagesRepository extends JpaRepository<Image, Long> {

    @Modifying
    @Query("DELETE FROM Image WHERE id not in (SELECT image from AccountDetails)")
    void deleteAllUnreferencedImages();
}
