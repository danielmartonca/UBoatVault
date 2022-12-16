package com.uboat.vault.api.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class LoggingUtils {
    private final static String logApiCallPrefix = "    API CALLED";
    private final static String logRequestPrefix = "    API PROCESSED";

    @Value("${uboat.pretty-print-logs}")
    private void setPrettyPrintLogs(String prettyPrintLogs) {
        LoggingUtils.PRETTY_PRINT_LOGS = prettyPrintLogs;
    }

    @Value("${uboat.apis-body-not-logged}")
    private void setApisNotLogged(String[] apisBodyNotLogged) {
        LoggingUtils.APIS_NOT_LOGGED = apisBodyNotLogged;
    }

    private static String PRETTY_PRINT_LOGS;
    private static String[] APIS_NOT_LOGGED;

    public static String colorString(String string, TextColor color) {
        return color.getColorCode() + string + TextColor.RESET.getColorCode();
    }

    public static String colorString(String string, HttpMethod requestMethod) {
        return TextColor.getMethodColor(requestMethod).getColorCode() + string + TextColor.RESET.getColorCode();
    }

    private static String getApiAsString(String api, String queryParams) {
        if (!Strings.isEmpty(queryParams)) return api + "?" + queryParams;
        return api;
    }

    private static String formatBodyString(String api, String body) {
        if (api.contains("swagger") || api.contains("api-docs"))
            return "";

        if (Arrays.stream(LoggingUtils.APIS_NOT_LOGGED).anyMatch(notLoggedApi -> notLoggedApi.contains(api)))
            return " body logging ignored";

        if ("true".equalsIgnoreCase(PRETTY_PRINT_LOGS))
            body = toStringFormatted(body);

        if (Strings.isEmpty(body)) return " no body";

        if (body.startsWith("�PNG")) return "\n[ 'bytes...' ]";
        body = '\n' + body;

        if (body.contains("bytes"))
            return body.replaceAll("(\"bytes\":\\s\".+\",)", "\"bytes:\"['bytes...']");

        return body;
    }

    public static void logApiCall(HttpMethod httpMethod, String api, String queryParams) {
        log.info(colorString("[{}] {} {}", httpMethod), httpMethod.toString(), getApiAsString(api, queryParams), logApiCallPrefix);
    }

    public static void logRequest(HttpMethod httpMethod, int status, String api, String queryParams, String requestBody, String responseBody) {
        log.info(colorString("[{}] {} {}\nREQUEST:{}\nRESPONSE <{}>:{}", httpMethod), httpMethod.toString(), getApiAsString(api, queryParams), logRequestPrefix, formatBodyString(api, requestBody), HttpStatus.valueOf(status), formatBodyString(api, responseBody));
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

        public static TextColor getMethodColor(HttpMethod requestMethod) {
            return switch (requestMethod) {
                case GET -> TextColor.BLUE;
                case POST -> TextColor.PURPLE;
                case PUT -> TextColor.YELLOW;
                case DELETE -> TextColor.CYAN;
                case PATCH -> TextColor.WHITE;
                default -> TextColor.RED;
            };
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
