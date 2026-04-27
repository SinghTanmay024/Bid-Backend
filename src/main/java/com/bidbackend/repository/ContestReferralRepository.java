package com.bidbackend.repository;

import com.bidbackend.model.ContestReferral;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContestReferralRepository extends MongoRepository<ContestReferral, String> {

    List<ContestReferral> findByContestIdAndReferrerId(String contestId, String referrerId);

    long countByContestIdAndReferrerId(String contestId, String referrerId);

    boolean existsByContestIdAndReferrerIdAndReferredUserId(
            String contestId, String referrerId, String referredUserId);
}
