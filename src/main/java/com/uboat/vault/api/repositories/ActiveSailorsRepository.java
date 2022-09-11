package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.sailing.sailor.ActiveSailor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActiveSailorsRepository extends JpaRepository<ActiveSailor, Long> {
    @Query(value = "SELECT * FROM [UBoatDB].[dbo].[active_sailors] WHERE (DATEDIFF(SECOND ,[last_update],GETDATE())) < :seconds AND [looking_for_clients]=1", nativeQuery = true)
    List<ActiveSailor> findAllFreeActiveSailors(@Param("seconds") int seconds);

    @Query(value = "SELECT * FROM [UBoatDB].[dbo].[active_sailors] WHERE (DATEDIFF(SECOND ,[last_update],GETDATE())) < :seconds AND [looking_for_clients]=1 AND id=:id ", nativeQuery = true)
    ActiveSailor findFreeActiveSailorById(@Param("seconds") int seconds, @Param("id") Long id);

    ActiveSailor findFirstByAccountId(Long accountId);
}
