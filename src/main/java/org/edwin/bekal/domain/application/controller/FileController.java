package org.edwin.bekal.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "File Management", description = "Endpoints for previewing and downloading uploaded application files and documents")
public class FileController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Operation(summary = "Serve / Download Uploaded File", description = "Streams or downloads an uploaded document or photo by its filename.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File served successfully"),
            @ApiResponse(responseCode = "404", description = "File not found or unreadable"),
            @ApiResponse(responseCode = "400", description = "Invalid filename path")
    })
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @Parameter(description = "Stored filename including extension", example = "550e8400-e29b-41d4-a716-446655440000_ktp.jpg")
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // ✅ Detect content type automatically
            String contentType = "application/octet-stream";
            String lowerName = filename.toLowerCase();
            if (lowerName.endsWith(".pdf"))  contentType = "application/pdf";
            else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (lowerName.endsWith(".png"))  contentType = "image/png";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
