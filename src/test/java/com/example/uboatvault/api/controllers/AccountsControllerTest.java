package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.model.persistence.account.Account;
import com.example.uboatvault.api.model.persistence.account.info.PhoneNumber;
import com.example.uboatvault.api.repositories.AccountsRepository;
import com.example.uboatvault.api.repositories.PhoneNumbersRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

class AccountsControllerTest extends ControllerTest {
    @Autowired
    private AccountsController controller;

    @MockBean
    private AccountsRepository accountsRepository;
    @MockBean
    private PhoneNumbersRepository phoneNumbersRepository;

    @ParameterizedTest
    @ValueSource(strings = {" !\"#$%&'()*+,/:;<=>?@[]\\^`{|}~"})
    void checkUsernameInvalidCharacters(String specialCharactersNotAllowed) {
        for (var ch : specialCharactersNotAllowed.toCharArray()) {
            var response = controller.checkUsername("test" + ch + "test");

            assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
            assertFalse(response.hasBody(), "Expected body to be null if username does not match pattern. Character tested is :'" + ch + "'");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"testusername"})
    void checkUsernameIfNotExisting(String username) {
        var response = controller.checkUsername(username);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
        assertEquals(Boolean.FALSE, response.getBody(), "Expected body of response to be true if the username is already used.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"testusername"})
    void checkUsernameIfExisting(String username) {
        when(accountsRepository.findFirstByUsername(username)).thenReturn(new Account());

        var response = controller.checkUsername(username);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
        assertEquals(Boolean.TRUE, response.getBody(), "Expected body of response to be true if the username is already used.");
    }


    @ParameterizedTest
    @CsvSource({"+40720000000,123,12"})
    void checkPhoneNumberIfNotExisting(String phoneNumber, String dialCode, String isoCode) {
        var response = controller.checkPhoneNumber(phoneNumber, dialCode, isoCode);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
        assertEquals(Boolean.FALSE, response.getBody(), "Expected body to be false if no phone number exists with the given data.");
    }

    @ParameterizedTest
    @CsvSource({"+40720000000,123,12"})
    void checkPhoneNumberIfExisting(String phoneNumber, String dialCode, String isoCode) {
        when(phoneNumbersRepository.findFirstByPhoneNumberAndDialCodeAndIsoCode(phoneNumber, dialCode, isoCode)).thenReturn(new PhoneNumber());
        var response = controller.checkPhoneNumber(phoneNumber, dialCode, isoCode);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
        assertEquals(Boolean.FALSE, response.getBody(), "Expected body to be false if no phone number exists with the given data.");
    }
}
