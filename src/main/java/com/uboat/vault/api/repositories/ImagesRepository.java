package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.account.info.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagesRepository extends JpaRepository<Image, Long> {
}
