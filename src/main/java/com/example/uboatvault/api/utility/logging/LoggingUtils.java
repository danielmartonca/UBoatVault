package com.example.uboatvault.api.utility.logging;

import com.example.uboatvault.api.model.persistence.Account;
import com.example.uboatvault.api.model.persistence.AccountDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.http.HttpMethod;

import java.util.Arrays;


public class LoggingUtils {
    static final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static String colorString(String string, TextColor color) {
        return color.getColorCode() + string + TextColor.RESET.getColorCode();
    }

    private static String colorsBasedData(String suffix, HttpMethod requestMethod, String api, Object body) {
        String bodyAsString;
        try {
            bodyAsString = "\n" + ow.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            bodyAsString = "";
        }
        if (body == null) bodyAsString = " with no body.";

        return switch (requestMethod) {
            case GET -> colorString('[' + requestMethod.toString() + "]      " + api + suffix + bodyAsString, TextColor.BLUE);
            case POST -> colorString('[' + requestMethod.toString() + "]      " + api + suffix + bodyAsString, TextColor.PURPLE);
            case PUT -> colorString('[' + requestMethod.toString() + "]      " + api + suffix + bodyAsString, TextColor.YELLOW);
            case DELETE -> colorString('[' + requestMethod.toString() + "]      " + api + suffix + bodyAsString, TextColor.CYAN);
            default -> colorString('[' + requestMethod.toString() + "]      " + api + suffix + bodyAsString + "\n      HTTP METHOD NOT SUPPORTED BY REST API", TextColor.RED);
        };
    }

    public static String logRequest(HttpMethod requestMethod, String api) {
        String suffix = "       REQUEST:";
        return colorsBasedData(suffix, requestMethod, api, null);
    }

    public static String logRequest(HttpMethod requestMethod, String api, Object body) {
        String suffix = "       REQUEST:";
        return colorsBasedData(suffix, requestMethod, api, body);
    }

    public static String logRequest(HttpMethod requestMethod, String api, Account requestAccount) {
        byte[] bytes;
        if (requestAccount.getAccountDetails() != null && requestAccount.getAccountDetails().getImage() != null && requestAccount.getAccountDetails().getImage().getBytes() != null && requestAccount.getAccountDetails().getImage().getBytes().length > 0) {
            var existingBytes = requestAccount.getAccountDetails().getImage().getBytes();
            bytes = Arrays.copyOf(existingBytes, existingBytes.length);
            requestAccount.getAccountDetails().getImage().setBytes(null);
            var returnedString = LoggingUtils.logRequest(HttpMethod.POST, "/api/updateAccountDetails", (Object) requestAccount);
            requestAccount.getAccountDetails().getImage().setBytes(bytes);
            return returnedString.replaceAll("\"bytes\" : null","\"bytes\" : [...bytes...]");
        }

        String suffix = "       REQUEST:";
        return colorsBasedData(suffix, requestMethod, api, requestAccount);
    }

    public static String logResponse(HttpMethod requestMethod, String api) {
        String suffix = "       RESPONSE:";
        return colorsBasedData(suffix, requestMethod, api, null);
    }

    public static String logResponse(HttpMethod requestMethod, String api, Object body) {
        String suffix = "       RESPONSE:";
        return colorsBasedData(suffix, requestMethod, api, body);
    }

    public static String logResponse(HttpMethod requestMethod, String api, AccountDetails accountDetails) {
        byte[] bytes;
        if (accountDetails != null && accountDetails.getImage() != null && accountDetails.getImage().getBytes() != null && accountDetails.getImage().getBytes().length > 0) {
            var existingBytes = accountDetails.getImage().getBytes();
            bytes = Arrays.copyOf(existingBytes, existingBytes.length);
            accountDetails.getImage().setBytes(null);
            var returnedString = LoggingUtils.logResponse(HttpMethod.POST, api, (Object) accountDetails);
            accountDetails.getImage().setBytes(bytes);
            return returnedString.replaceAll("\"bytes\" : null","\"bytes\" : [...bytes...]");
        }
        String suffix = "       REQUEST:";
        return colorsBasedData(suffix, requestMethod, api, accountDetails);
    }

    public static String logResponse(HttpMethod requestMethod, String api, Account responseAccount) {
        byte[] bytes;
        if (responseAccount.getAccountDetails() != null && responseAccount.getAccountDetails().getImage() != null && responseAccount.getAccountDetails().getImage().getBytes() != null && responseAccount.getAccountDetails().getImage().getBytes().length > 0) {
            var existingBytes = responseAccount.getAccountDetails().getImage().getBytes();
            bytes = Arrays.copyOf(existingBytes, existingBytes.length);
            responseAccount.getAccountDetails().getImage().setBytes(null);
            var returnedString = LoggingUtils.logResponse(HttpMethod.POST, "/api/updateAccountDetails", (Object) responseAccount);
            responseAccount.getAccountDetails().getImage().setBytes(bytes);
            return returnedString.replaceAll("\"bytes\" : null","\"bytes\" : [...bytes...]");
        }
        String suffix = "       REQUEST:";
        return colorsBasedData(suffix, requestMethod, api, responseAccount);
    }
}
