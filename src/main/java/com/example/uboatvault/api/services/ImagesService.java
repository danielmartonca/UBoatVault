package com.example.uboatvault.api.services;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ImagesService {
    private final Logger log = LoggerFactory.getLogger(ImagesService.class);

    public byte[] getDefaultProfilePicture() {
        try {
            File initialFile = new File("src/main/resources/static/default_profile_pic.png");
            if (!initialFile.exists())
                throw new IOException("File doesn't exist.");
            InputStream in = new FileInputStream(initialFile);
            log.info("Returning default profile picture.");
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            log.error("Exception while retrieving default profile picture.", e);
        }
        return null;
    }
}
