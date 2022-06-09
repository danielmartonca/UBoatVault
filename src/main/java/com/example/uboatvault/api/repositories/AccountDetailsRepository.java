package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.account.info.AccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountDetailsRepository extends JpaRepository<AccountDetails, Long> {
    @Query(value = "SELECT image_id FROM accounts_details where accounts_details.id=:accountDetailsId",nativeQuery = true)
    Long findImageIdByAccountDetailsId(@Param("accountDetailsId")Long accountDetailsId);
}
