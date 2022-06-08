package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.location.ActiveSailor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActiveSailorsRepository extends JpaRepository<ActiveSailor, Long> {
    @Query(value = "SELECT * FROM [UBoatDB].[dbo].[active_sailors] WHERE (DATEDIFF(SECOND ,[last_update],GETDATE())) < :seconds", nativeQuery = true)
    List<ActiveSailor> findAllFreeActiveSailors(@Param("seconds") int seconds);

    ActiveSailor findFirstByAccountId(Long accountId);
}
