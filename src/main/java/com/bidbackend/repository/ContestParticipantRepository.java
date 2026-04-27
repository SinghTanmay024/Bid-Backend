package com.bidbackend.repository;

import com.bidbackend.model.ContestParticipant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ContestParticipantRepository extends MongoRepository<ContestParticipant, String> {

    List<ContestParticipant> findByContestId(String contestId);

    Optional<ContestParticipant> findByContestIdAndUserId(String contestId, String userId);

    boolean existsByContestIdAndUserId(String contestId, String userId);

    /** Fraud detection: same IP entering a contest multiple times. */
    List<ContestParticipant> findByContestIdAndIpAddress(String contestId, String ipAddress);

    /** Fraud detection: all entries by a user since a given time (cross-contest). */
    List<ContestParticipant> findByUserIdAndEnteredAtAfter(String userId, Instant since);

    /** Count referrals made by a user within a given window. */
    long countByReferredByAndEnteredAtAfter(String referredBy, Instant since);
}
