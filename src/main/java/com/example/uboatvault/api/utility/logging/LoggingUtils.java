package com.example.uboatvault.api.utility.logging;

import org.springframework.http.HttpMethod;


public class LoggingUtils {
    public static String logRequestAsString(HttpMethod requestMethod, String api,Object body) {
        return switch (requestMethod) {
            case GET -> (TextColorEnum.GREEN.getColorCode() + '[' + requestMethod + "]      " + api + "\n"+body+ TextColorEnum.RESET.getColorCode());
            case POST -> (TextColorEnum.PURPLE.getColorCode() + '[' + requestMethod + "]     " + api + "\n"+body+ TextColorEnum.RESET.getColorCode());
            case PUT -> (TextColorEnum.YELLOW.getColorCode() + '[' + requestMethod + "]      " + api + "\n"+body+ TextColorEnum.RESET.getColorCode());
            case DELETE -> (TextColorEnum.CYAN.getColorCode() + '[' + requestMethod + "]   " + api + "\n"+body+ TextColorEnum.RESET.getColorCode());
            default -> (TextColorEnum.RED.getColorCode() + '[' + requestMethod + "]       " + api + "\n"+body+ "    NOT IMPLEMENTED YET" + TextColorEnum.RESET.getColorCode());
        };
    }
}
