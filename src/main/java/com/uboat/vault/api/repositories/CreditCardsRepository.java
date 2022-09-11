package com.uboat.vault.api.repositories;

import com.uboat.vault.api.model.persistence.account.info.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditCardsRepository extends JpaRepository<CreditCard, Long> {
    CreditCard findFirstByNumberAndCvc(String number, String cvc);
    void deleteByNumberAndCvc(String number,String cvc);
}
