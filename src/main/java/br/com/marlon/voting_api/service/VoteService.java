package br.com.marlon.voting_api.service;

import br.com.marlon.voting_api.client.CpfEligibilityClient;
import br.com.marlon.voting_api.dto.request.CastVoteRequest;
import br.com.marlon.voting_api.entity.Vote;
import br.com.marlon.voting_api.entity.VotingSession;
import br.com.marlon.voting_api.repository.VoteRepository;
import br.com.marlon.voting_api.repository.VotingSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final VotingSessionRepository votingSessionRepository;
    private final CpfEligibilityClient cpfEligibilityClient;

    public VoteService(
            VoteRepository voteRepository,
            VotingSessionRepository votingSessionRepository,
            CpfEligibilityClient cpfEligibilityClient
    ) {
        this.voteRepository = voteRepository;
        this.votingSessionRepository = votingSessionRepository;
        this.cpfEligibilityClient = cpfEligibilityClient;
    }

    @Transactional
    public Vote cast(CastVoteRequest request) {
        VotingSession votingSession = votingSessionRepository
                .findById(request.votingSessionId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Voting session not found")
                );
        OffsetDateTime now = OffsetDateTime.now();

        boolean isSessionOpen = !now.isBefore(votingSession.getOpenedAt())
                && now.isBefore(votingSession.getClosesAt());

        if (!isSessionOpen) {
            throw new IllegalStateException("Voting session is not open");
        }

        String associateCpf = request.associateCpf().replaceAll("\\D", "");

        if (voteRepository.existsByVotingSessionIdAndAssociateCpf(
                votingSession.getId(),
                associateCpf
        )) {
            throw new IllegalStateException(
                    "Associate has already voted in this voting session"
            );
        }

//        VoteEligibility eligibility = cpfEligibilityClient
//                .checkEligibility(associateCpf);
//
//        if (eligibility != VoteEligibility.ABLE_TO_VOTE) {
//            throw new IllegalStateException(
//                    "Associate is not eligible to vote"
//            );
//        }

        Vote vote = new Vote();
        vote.setVotingSession(votingSession);
        vote.setAssociateCpf(associateCpf);
        vote.setChoice(request.choice());

        return voteRepository.save(vote);
    }
}
