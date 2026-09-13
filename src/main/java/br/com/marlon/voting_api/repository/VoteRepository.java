package br.com.marlon.voting_api.repository;

import br.com.marlon.voting_api.entity.Vote;
import br.com.marlon.voting_api.entity.VoteChoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByVotingSessionIdAndAssociateCpf(
            Long votingSessionId,
            String associateCpf
    );

    long countByVotingSessionIdAndChoice(
            Long votingSessionId,
            VoteChoice choice
    );
}



