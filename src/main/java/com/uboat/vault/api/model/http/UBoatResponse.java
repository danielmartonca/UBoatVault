package com.uboat.vault.api.model.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uboat.vault.api.model.enums.UBoatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude
public class UBoatResponse {
    private final UBoatStatus header;
    private final Object body;
}
