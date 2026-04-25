package com.bidbackend.controller;

import com.bidbackend.dto.UploadResponse;
import com.bidbackend.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "Image upload to Cloudinary")
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an image and get back a hosted URL",
               description = "Accepts multipart/form-data with field name 'file'. Returns Cloudinary CDN URL.")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        String url = uploadService.upload(file);
        return ResponseEntity.ok(new UploadResponse(url));
    }
}
