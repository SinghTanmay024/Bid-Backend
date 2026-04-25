package com.bidbackend.service;

import com.bidbackend.dto.ContactRequest;
import com.bidbackend.model.ContactMessage;
import com.bidbackend.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    public void submit(ContactRequest request) {
        ContactMessage message = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .message(request.getMessage())
                .submittedAt(LocalDateTime.now())
                .build();

        contactRepository.save(message);
    }
}
