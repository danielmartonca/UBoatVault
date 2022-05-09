package com.example.uboatvault.api.security;

public class EncryptionManager {
    public static String encrypt(String plaintext) {
        return plaintext;
    }
    public static String decrypt(String cipherText) {
        return cipherText;
    }

    public static String encrypt(Integer plaintext) {
        return encrypt(plaintext.toString());
    }


    public static Integer decrypt(Integer cipherText) {
        return Integer.parseInt(decrypt(cipherText.toString()));
    }
}
