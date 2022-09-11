package com.uboat.vault.api.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SailorConnectionRequest {
   JourneyRequest journeyRequest;
   String sourceAddress;
   String destinationAddress;
   String sailorId;
}
