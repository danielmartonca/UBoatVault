package com.uboat.vault.api.utilities;

import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static String loadStaticHtmlTemplate(String fileName) throws IOException {
        var file = ResourceUtils.getFile("classpath:static/" + fileName);
        try (var is = new FileInputStream(file)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
