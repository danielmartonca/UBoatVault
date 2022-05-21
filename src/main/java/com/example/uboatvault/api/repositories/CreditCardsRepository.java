package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditCardsRepository extends JpaRepository<CreditCard, Long> {
    CreditCard findFirstByBankAndNumberAndCvc(String bank, String number, String cvc);
}