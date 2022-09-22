package com.uboat.vault.api.model.http;

import com.uboat.vault.api.model.persistence.account.info.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestImage {
    private byte[] bytes;
    private String hash;

    public RequestImage(Image image) {
        this.bytes = image.getBytes();
        this.hash = image.getHash();
    }
}
