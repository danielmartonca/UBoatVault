package com.uboat.vault.api.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class LoggingUtils {
    private static final Logger log = LoggerFactory.getLogger(LoggingUtils.class);

    @Value("${uboat.pretty-print-logs}")
    private void setPrettyPrintLogs(String prettyPrintLogs) {
        LoggingUtils.PRETTY_PRINT_LOGS = prettyPrintLogs;
    }

    private static String PRETTY_PRINT_LOGS;

    public static String colorString(String string, TextColor color) {
        return color.getColorCode() + string + TextColor.RESET.getColorCode();
    }

    private static String colorsBasedData(String suffix, HttpMethod requestMethod, String api, String queryParams, String body) {
        if (body == null || body.isEmpty()) body = " no body";
        else if (body.startsWith("�PNG")) body = "\n[ bytes... ]";
        else
            body = '\n' + body;
        if (body.contains("bytes")) body = body.replace("(\"bytes\":\".*\")", "\"bytes:\"[ bytes... ]");

        if (queryParams != null && !queryParams.isBlank()) api = api + "?" + queryParams;

        final String str = '[' + requestMethod.toString() + "]      " + api + suffix + body;
        return switch (requestMethod) {
            case GET -> colorString(str, TextColor.BLUE);
            case POST -> colorString(str, TextColor.PURPLE);
            case PUT -> colorString(str, TextColor.YELLOW);
            case DELETE -> colorString(str, TextColor.CYAN);
            default ->
                    colorString('[' + requestMethod.toString() + "]      " + api + suffix + body + "\n      HTTP METHOD NOT SUPPORTED BY REST API", TextColor.RED);
        };
    }

    public static void logRequest(HttpMethod requestMethod, String api, String queryParams, String body) {
        if (api.contains("swagger") || api.contains("api-docs"))
            body = "";

        if (PRETTY_PRINT_LOGS != null && PRETTY_PRINT_LOGS.equalsIgnoreCase("true"))
            body = toStringFormatted(body);

        String suffix = "       REQUEST:";
        log.info(colorsBasedData(suffix, requestMethod, api, queryParams, body));
    }

    public static void logResponse(HttpMethod requestMethod, String api, String queryParams, String body) {
        if (api.contains("swagger") || api.contains("api-docs"))
            body = "";

        if (PRETTY_PRINT_LOGS != null && PRETTY_PRINT_LOGS.equalsIgnoreCase("true"))
            body = toStringFormatted(body);

        String suffix = "       RESPONSE:";
        log.info(colorsBasedData(suffix, requestMethod, api, queryParams, body));
    }

    public enum TextColor {
        RESET("\u001B[0m"),
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m");

        private final String color;

        TextColor(String color) {
            this.color = color;
        }

        public String getColorCode() {
            return color;
        }
    }

    public static String toStringFormatted(Object object) {
        try {
            var ow = new ObjectMapper()
                    .writer()
                    .withDefaultPrettyPrinter();
            return ow.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to print object of type {}.", object.getClass().getSimpleName(), e);
            return object.toString();
        }
    }

    public static String toStringFormatted(String string) {
        try {
            if (string == null || string.isEmpty()) return string;
            var jsonObject = new JSONObject(string);
            return jsonObject.toString(4);
        } catch (JSONException e) {
            return string;
        }
    }
}
