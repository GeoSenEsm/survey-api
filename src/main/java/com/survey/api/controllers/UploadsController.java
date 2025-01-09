package com.survey.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@RestController
@RequestMapping("/uploads")
@Tag(name = "Uploaded images", description = "Provides access to uploaded files.")
public class UploadsController {
    private final Path basePath = Paths.get("uploads");

    @GetMapping("/**")
    @Operation(
            summary = "Retrieve uploaded file.",
            description = """
                    - Fetches an uploaded image from the server based on the requested path.
                    - Those images are a part of image question type.
                    - Supported file types include images with extensions `.png`, `.jpg`, and `.jpeg`.
                    - **Access:**
                        - unrestricted
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "File retrieved successfully.",
                    content = {@Content(mediaType = "application/octet-stream")}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "File not found.",
                    content = @Content
            )
    })
    public ResponseEntity<Resource> getImage() throws MalformedURLException {
        String pathFromRequest = extractPathFromRequest();

        Path filePath = basePath.resolve(Paths.get(pathFromRequest)).normalize();

        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            String contentType = getContentType(filePath);
            return ResponseEntity.ok()
                    .contentType(contentType != null ? org.springframework.http.MediaType.parseMediaType(contentType) : org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    private String extractPathFromRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String requestURI = request.getRequestURI();
        return java.net.URLDecoder.decode(requestURI, StandardCharsets.UTF_8);
    }

    private String getContentType(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return null;
    }
}
