package com.uboat.vault.api.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uboat.vault.api.model.enums.UBoatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude
public class UBoatDTO {
    private final UBoatStatus header;
    private Object body;
}
