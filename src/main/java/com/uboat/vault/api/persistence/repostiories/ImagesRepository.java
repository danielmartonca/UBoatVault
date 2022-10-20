package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.info.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagesRepository extends JpaRepository<Image, Long> {
}
