package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.sailing.sailor.Sailor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SailorsRepository extends JpaRepository<Sailor, Long> {
    List<Sailor> findAllByLookingForClients(boolean lookingForClients);
    Sailor findSailorByIdAndLookingForClientsIsTrue(@Param("id") Long id);
    Sailor findFirstByAccountId(Long accountId);
}
