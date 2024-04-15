package com.ss871104.oauth2security.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {
    private static final Logger logger = LoggerFactory.getLogger(DefaultController.class);

    @GetMapping("/favicon.ico")
    public void returnNoFavicon() {
    }

    @GetMapping("/error")
    public ResponseEntity error() {
        logger.error("GG, the system suck!");

        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }
}
