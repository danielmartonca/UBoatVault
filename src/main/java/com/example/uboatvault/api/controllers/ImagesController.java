package com.example.uboatvault.api.controllers;

import com.example.uboatvault.api.services.ImagesService;
import com.example.uboatvault.api.utility.logging.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class ImagesController {
    private final Logger log = LoggerFactory.getLogger(ImagesController.class);
    private final ImagesService imagesService;

    public ImagesController(ImagesService imagesService) {
        this.imagesService = imagesService;
    }


    @GetMapping(value = "/images/getDefaultProfilePicture", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody
    byte[] getDefaultProfilePicture() {
        log.info(LoggingUtils.logRequest(HttpMethod.GET, "/api/getDefaultProfilePicture"));
        return imagesService.getDefaultProfilePicture();
    }
}
