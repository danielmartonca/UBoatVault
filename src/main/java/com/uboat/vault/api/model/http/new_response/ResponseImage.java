package com.uboat.vault.api.model.http.new_response;

import com.uboat.vault.api.model.persistence.account.info.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseImage {
    private byte[] bytes;

    //TODO
    //    private String hash;

    public ResponseImage(Image image) {
        this.bytes = image.getBytes();
        //this.hash=image.getHash();
    }
}
