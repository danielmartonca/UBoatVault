package com.example.uboatvault.api.utility.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.http.HttpMethod;


public class LoggingUtils {
    static final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static String logRequestAsString(HttpMethod requestMethod, String api, Object body) {
        String bodyAsString;
        try {
            bodyAsString = "\n" + ow.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            bodyAsString = "";
        }
        if (body == null) bodyAsString = " with no body.";

        return switch (requestMethod) {
            case GET -> (TextColorEnum.GREEN.getColorCode() + '[' + requestMethod + "]      " + api + TextColorEnum.RESET.getColorCode());
            case POST -> (TextColorEnum.PURPLE.getColorCode() + '[' + requestMethod + "]     " + api + bodyAsString + TextColorEnum.RESET.getColorCode());
            case PUT -> (TextColorEnum.YELLOW.getColorCode() + '[' + requestMethod + "]      " + api + bodyAsString + TextColorEnum.RESET.getColorCode());
            case DELETE -> (TextColorEnum.CYAN.getColorCode() + '[' + requestMethod + "]   " + api + bodyAsString + TextColorEnum.RESET.getColorCode());
            default -> (TextColorEnum.RED.getColorCode() + '[' + requestMethod + "]       " + api + bodyAsString + "\n    NOT IMPLEMENTED YET" + TextColorEnum.RESET.getColorCode());
        };
    }
}
