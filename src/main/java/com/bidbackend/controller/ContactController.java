package com.bidbackend.controller;

import com.bidbackend.dto.ContactRequest;
import com.bidbackend.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "Contact form submission")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @Operation(summary = "Submit a contact form message")
    public ResponseEntity<Map<String, String>> submit(@Valid @RequestBody ContactRequest request) {
        contactService.submit(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "Your message has been received. We'll get back to you soon!"));
    }
}
