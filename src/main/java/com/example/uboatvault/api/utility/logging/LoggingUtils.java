package com.example.uboatvault.api.utility.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;


public class LoggingUtils {
    private static final Logger log = LoggerFactory.getLogger(LoggingUtils.class);

    public static String colorString(String string, TextColor color) {
        return color.getColorCode() + string + TextColor.RESET.getColorCode();
    }

    private static String colorsBasedData(String suffix, HttpMethod requestMethod, String api, String body) {
        if (body == null || body.isEmpty()) body = " no body";
        if(body.startsWith("�PNG")) body = "[ bytes... ]";

        final String str = '[' + requestMethod.toString() + "]      " + api + suffix + body;
        return switch (requestMethod) {
            case GET -> colorString(str, TextColor.BLUE);
            case POST -> colorString(str, TextColor.PURPLE);
            case PUT -> colorString(str, TextColor.YELLOW);
            case DELETE -> colorString(str, TextColor.CYAN);
            default -> colorString('[' + requestMethod.toString() + "]      " + api + suffix + body + "\n      HTTP METHOD NOT SUPPORTED BY REST API", TextColor.RED);
        };
    }

    public static void logRequest(HttpMethod requestMethod, String api, String body) {
        String suffix = "       REQUEST:\n";
        log.info(colorsBasedData(suffix, requestMethod, api, body));
    }

    public static void logResponse(HttpMethod requestMethod, String api, String body) {
        String suffix = "       RESPONSE:\n";
        log.info(colorsBasedData(suffix, requestMethod, api, body));
    }
}
