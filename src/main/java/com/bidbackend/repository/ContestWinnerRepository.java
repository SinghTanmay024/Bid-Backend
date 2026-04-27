package com.bidbackend.repository;

import com.bidbackend.model.ContestWinner;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContestWinnerRepository extends MongoRepository<ContestWinner, String> {

    List<ContestWinner> findByContestId(String contestId);

    boolean existsByContestId(String contestId);
}
