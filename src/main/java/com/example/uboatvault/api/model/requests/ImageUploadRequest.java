package com.example.uboatvault.api.model.requests;

import com.example.uboatvault.api.model.persistence.Account;
import lombok.Data;


@Data
public class ImageUploadRequest {
    private final Account account;
    private final byte[] imageBytes;

    public ImageUploadRequest(Account account, byte[] imageBytes) {
        this.account = account;
        this.imageBytes = imageBytes;
    }

    @Override
    public String toString() {
        return "ImageUploadRequest{" +
                "account=" + account +
                ", imageBytes=" + " ...bytes..." +
                '}';
    }
}
