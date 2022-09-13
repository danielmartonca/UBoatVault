package com.uboat.vault.api.model.exceptions;

import com.uboat.vault.api.model.enums.UBoatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UBoatJwtException extends Exception {
    private UBoatStatus status = UBoatStatus.VAULT_INTERNAL_SERVER_ERROR;
}
