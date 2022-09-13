package com.uboat.vault.api.model.http.new_response;

import com.uboat.vault.api.model.persistence.account.info.AccountDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAccountDetails {
    private String fullName;
    private String email;
    private ResponseImage image;

    public ResponseAccountDetails(AccountDetails accountDetails) {
        this.fullName = accountDetails.getFullName();
        this.email = accountDetails.getEmail();
        this.image = new ResponseImage(accountDetails.getImage());
    }
}
