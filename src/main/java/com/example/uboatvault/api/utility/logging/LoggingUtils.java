package com.example.uboatvault.api.utility.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.http.HttpMethod;


public class LoggingUtils {
    static  final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    public static String logRequestAsString(HttpMethod requestMethod, String api, Object body) {
        String bodyAsString;
        try {
            bodyAsString=ow.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            bodyAsString=null;
        }
        return switch (requestMethod) {
            case GET -> (TextColorEnum.GREEN.getColorCode() + '[' + requestMethod + "]      " + api +'\n'+ bodyAsString + TextColorEnum.RESET.getColorCode());
            case POST -> (TextColorEnum.PURPLE.getColorCode() + '[' + requestMethod + "]     " + api +'\n'+ bodyAsString + TextColorEnum.RESET.getColorCode());
            case PUT -> (TextColorEnum.YELLOW.getColorCode() + '[' + requestMethod + "]      " + api +'\n'+ bodyAsString + TextColorEnum.RESET.getColorCode());
            case DELETE -> (TextColorEnum.CYAN.getColorCode() + '[' + requestMethod + "]   " + api +'\n'+ bodyAsString + TextColorEnum.RESET.getColorCode());
            default -> (TextColorEnum.RED.getColorCode() + '[' + requestMethod + "]       " + api +'\n'+ bodyAsString + "    NOT IMPLEMENTED YET" + TextColorEnum.RESET.getColorCode());
        };
    }
}
