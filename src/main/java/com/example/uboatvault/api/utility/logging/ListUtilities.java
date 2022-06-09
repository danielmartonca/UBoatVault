package com.example.uboatvault.api.utility.logging;

import com.example.uboatvault.api.model.persistence.sailing.sailor.ActiveSailor;
import com.example.uboatvault.api.services.AccountsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Objects;

public class ListUtilities {
    private static final Logger log = LoggerFactory.getLogger(AccountsService.class);

    public static Pair<ActiveSailor, Double> findPairByActiveSailorAccountId(Long accountId, List<Pair<ActiveSailor, Double>> list, int startIndex) {
        for (int i = startIndex; i < list.size(); i++) {
            var pair = list.get(i);
            if (Objects.equals(pair.getFirst().getAccountId(), accountId))
                return pair;
        }
        log.warn("Couldn't find match in the list for account id: " + accountId);
        return null;
    }
}
