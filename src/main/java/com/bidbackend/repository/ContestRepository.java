package com.bidbackend.repository;

import com.bidbackend.model.Contest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface ContestRepository extends MongoRepository<Contest, String> {

    List<Contest> findByStatus(Contest.ContestStatus status);

    /** For scheduler: contests to open (UPCOMING and startTime has passed). */
    List<Contest> findByStatusAndStartTimeBefore(Contest.ContestStatus status, Instant now);

    /** For scheduler: contests to close (OPEN and endTime has passed). */
    List<Contest> findByStatusAndEndTimeBefore(Contest.ContestStatus status, Instant now);

    /** For transparency listing: contests that have been drawn (drawnAt is not null). */
    List<Contest> findByDrawnAtIsNotNullOrderByDrawnAtDesc();
}
