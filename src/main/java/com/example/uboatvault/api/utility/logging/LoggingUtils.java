package com.example.uboatvault.api.utility.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.http.HttpMethod;


public class LoggingUtils {
    static final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static String colorString(String string, TextColor color) {
        return color.getColorCode() + string + TextColor.RESET.getColorCode();
    }


    public static String logRequest(HttpMethod requestMethod, String api, Object body) {
        String bodyAsString;
        try {
            bodyAsString = "\n" + ow.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            bodyAsString = "";
        }
        if (body == null) bodyAsString = " with no body.";

        return switch (requestMethod) {
            case GET -> colorString('[' + requestMethod.toString() + "]      " + api + bodyAsString, TextColor.BLUE);
            case POST -> colorString('[' + requestMethod.toString() + "]      " + api + bodyAsString, TextColor.PURPLE);
            case PUT -> colorString('[' + requestMethod.toString() + "]      " + api + bodyAsString, TextColor.YELLOW);
            case DELETE -> colorString('[' + requestMethod.toString() + "]      " + api + bodyAsString, TextColor.CYAN);
            default -> colorString('[' + requestMethod.toString() + "]      " + api + bodyAsString + "\n      HTTP METHOD NOT SUPPORTED BY REST API", TextColor.RED);
        };
    }
}
