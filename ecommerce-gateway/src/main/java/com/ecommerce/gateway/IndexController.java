package com.ecommerce.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;

@Controller
public class IndexController {

    @Value("${frontend.dist:frontend/dist}")
    private String frontendDist;

    @GetMapping("/")
    public ResponseEntity<Resource> index() {
        File indexFile = new File(frontendDist, "index.html");
        if (indexFile.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(new FileSystemResource(indexFile));
        }
        return ResponseEntity.notFound().build();
    }
}