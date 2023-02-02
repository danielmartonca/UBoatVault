package com.uboat.vault.api.model.exceptions;

public class SmsVerificationServiceException extends Exception {
    private final Exception rootException;

    public SmsVerificationServiceException(Exception e) {
        this.rootException = e;
    }
}
