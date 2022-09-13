package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.sailing.sailor.Sailor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SailorsRepository extends JpaRepository<Sailor, Long> {
    //TODO - remove all native queries
    @Query(value = "SELECT * FROM [UBoatDB].[dbo].[active_sailors] WHERE (DATEDIFF(SECOND ,[last_update],GETDATE())) < :seconds AND [looking_for_clients]=1", nativeQuery = true)
    List<Sailor> findAllFreeActiveSailors(@Param("seconds") int seconds);

    //TODO - remove all native queries
    @Query(value = "SELECT * FROM [UBoatDB].[dbo].[active_sailors] WHERE (DATEDIFF(SECOND ,[last_update],GETDATE())) < :seconds AND [looking_for_clients]=1 AND id=:id ", nativeQuery = true)
    Sailor findFreeActiveSailorById(@Param("seconds") int seconds, @Param("id") Long id);

    Sailor findFirstByAccountId(Long accountId);
}
