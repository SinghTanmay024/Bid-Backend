package com.bidbackend.repository;

import com.bidbackend.model.ContactMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContactRepository extends MongoRepository<ContactMessage, String> {
}
