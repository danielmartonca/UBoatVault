package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.account.sailor.Sailor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;

public interface SailorsRepository extends JpaRepository<Sailor, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Sailor> findAllByLookingForClients(boolean lookingForClients);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Sailor findSailorByIdAndLookingForClientsIsTrue(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Sailor findFirstByAccountId(Long accountId);
}
