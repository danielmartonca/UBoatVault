package com.example.uboatvault.api.repositories;

import com.example.uboatvault.api.model.persistence.account.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditCardsRepository extends JpaRepository<CreditCard, Long> {
    CreditCard findFirstByNumberAndCvc(String number, String cvc);
    void deleteByNumberAndCvc(String number,String cvc);
}
