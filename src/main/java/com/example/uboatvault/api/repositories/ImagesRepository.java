package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagesRepository extends JpaRepository<Image,Long> {
}
