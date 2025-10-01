package com.scm.contactmanager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import org.springframework.core.io.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class FaviconController {

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping(value = "favicon.ico", produces = "image/x-icon")
    @ResponseBody
    public ResponseEntity<Resource> getFavicon() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:static/img/favicon.ico");
        return ResponseEntity.ok().body(resource);
    }
}