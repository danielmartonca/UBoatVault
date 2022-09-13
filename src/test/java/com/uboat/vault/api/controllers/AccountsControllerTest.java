package com.uboat.vault.api.controllers;

import com.uboat.vault.api.model.enums.UBoatStatus;
import com.uboat.vault.api.repositories.AccountsRepository;
import com.uboat.vault.api.repositories.PhoneNumbersRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
            var body = response.getBody();

            assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode(), "Invalid status code returned.");
            assert body != null;
            assertEquals(body.getHeader(), UBoatStatus.USERNAME_INVALID_FORMAT,"Expected body to be null if username does not match pattern. Character tested is :'" + ch + "'");
        }
    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {"testusername"})
//    void checkUsernameIfNotExisting(String username) {
//        var response = controller.checkUsername(username);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
//        assertEquals(Boolean.FALSE, response.getBody(), "Expected body of response to be true if the username is already used.");
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {"testusername"})
//    void checkUsernameIfExisting(String username) {
//        when(accountsRepository.findFirstByUsername(username)).thenReturn(new Account());
//
//        var response = controller.checkUsername(username);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
//        assertEquals(Boolean.TRUE, response.getBody(), "Expected body of response to be true if the username is already used.");
//    }
//
//    @ParameterizedTest
//    @CsvSource({"+40720000000,123,12"})
//    void checkPhoneNumberIfNotExisting(String phoneNumber, String dialCode, String isoCode) {
//        var response = controller.checkPhoneNumber(phoneNumber, dialCode, isoCode);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
//        assertEquals(Boolean.FALSE, response.getBody(), "Expected body to be false if no phone number exists with the given data.");
//    }
//
//    @ParameterizedTest
//    @CsvSource({"+40720000000,123,12"})
//    void checkPhoneNumberIfExisting(String phoneNumber, String dialCode, String isoCode) {
//        when(phoneNumbersRepository.findFirstByPhoneNumberAndDialCodeAndIsoCode(phoneNumber, dialCode, isoCode)).thenReturn(new PhoneNumber());
//        var response = controller.checkPhoneNumber(phoneNumber, dialCode, isoCode);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
//        assertEquals(Boolean.FALSE, response.getBody(), "Expected body to be false if no phone number exists with the given data.");
//    }
//
//    @ParameterizedTest
//    @CsvSource({"testAccount,testPassword,+40720000000,123,12"})
//    void getMissingAccountInformationTestAllCredentialCombinations(String username, String password, String phoneNumber, String dialCode, String isoCode) {
//        var account = Account.builder()
//                .username(username)
//                .password(password)
//                .phoneNumber(PhoneNumber.builder()
//                        .phoneNumber(phoneNumber)
//                        .dialCode(dialCode)
//                        .isoCode(isoCode)
//                        .build())
//                .build();
//
//        when(accountsRepository.findFirstByUsernameAndPassword(username, password)).thenReturn(account);
//        when(accountsRepository.findFirstByPhoneNumber_PhoneNumberAndPassword(phoneNumber, password)).thenReturn(account);
//
//        var testAccount = Account.builder()
//                .username(username)
//                .password(password + "_invalid")
//                .phoneNumber(PhoneNumber.builder()
//                        .phoneNumber(phoneNumber + "_invalid")
//                        .dialCode(dialCode)
//                        .isoCode(isoCode)
//                        .build())
//                .build();
//
//        var response = controller.getMissingAccountInformation(testAccount);
//        assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
//        Assertions.assertNull(response.getBody(), "Expected body to be null if credentials are invalid.");
//
//        testAccount = Account.builder()
//                .username(username)
//                .password(password)
//                .phoneNumber(PhoneNumber.builder()
//                        .phoneNumber(phoneNumber + "_invalid")
//                        .dialCode(dialCode)
//                        .isoCode(isoCode)
//                        .build())
//                .build();
//
//        response = controller.getMissingAccountInformation(testAccount);
//        assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
//        Assertions.assertEquals(account, response.getBody(), "Expected account to be found if username and password match but phone number does not.");
//
//        testAccount = Account.builder()
//                .username(username + "_invalid")
//                .password(password)
//                .phoneNumber(PhoneNumber.builder()
//                        .phoneNumber(phoneNumber)
//                        .dialCode(dialCode)
//                        .isoCode(isoCode)
//                        .build())
//                .build();
//
//        response = controller.getMissingAccountInformation(testAccount);
//        assertEquals(HttpStatus.OK, response.getStatusCode(), "Invalid status code returned.");
//        Assertions.assertEquals(account, response.getBody(), "Expected account to be found if phone number and password match but username does not.");
//    }
}
