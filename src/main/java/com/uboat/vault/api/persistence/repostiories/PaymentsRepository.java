package com.uboat.vault.api.persistence.repostiories;

import com.uboat.vault.api.model.domain.sailing.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentsRepository extends JpaRepository<Payment, Long> {
}
