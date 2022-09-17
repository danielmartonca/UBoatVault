package com.uboat.vault.api.model.http.requests;

import com.uboat.vault.api.model.http.new_requests.RequestNewJourney;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SailorConnectionRequest {
   RequestNewJourney journeyRequest;
   String sourceAddress;
   String destinationAddress;
   String sailorId;
}
