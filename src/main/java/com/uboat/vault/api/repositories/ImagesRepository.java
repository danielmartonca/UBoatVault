package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.account.info.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImagesRepository extends JpaRepository<Image, Long> {
    //TODO - remove all native queries
    @Query(value = "SELECT * FROM Images WHERE account_details_id=:accountDetailsId", nativeQuery = true)
    Image findByAccountDetailsIdNative(@Param("accountDetailsId") Long accountDetailsId);
}
