package com.ecommerce.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;

@Controller
public class SpaErrorController implements ErrorController {

    @Value("${frontend.dist:frontend/dist}")
    private String frontendDist;

    @RequestMapping("/error")
    public ResponseEntity<?> handleError(HttpServletRequest request) {
        String originalUri = (String) request.getAttribute("jakarta.servlet.forward.request_uri");
        String accept = request.getHeader("Accept");

        if (originalUri != null && originalUri.startsWith("/api/")) {
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"code\":500,\"message\":\"Internal Server Error\"}");
        }

        if (accept != null && accept.contains("application/json")) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"code\":404,\"message\":\"Not Found\"}");
        }

        File indexFile = new File(frontendDist, "index.html");
        if (indexFile.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(new FileSystemResource(indexFile));
        }

        return ResponseEntity.notFound().build();
    }
}