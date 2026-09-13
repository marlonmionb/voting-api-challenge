package br.com.marlon.voting_api.service;

import br.com.marlon.voting_api.dto.request.OpenVotingSessionRequest;
import br.com.marlon.voting_api.dto.response.VotingResultResponse;
import br.com.marlon.voting_api.entity.AgendaItem;
import br.com.marlon.voting_api.entity.VoteChoice;
import br.com.marlon.voting_api.entity.VotingSession;
import br.com.marlon.voting_api.repository.AgendaItemRepository;
import br.com.marlon.voting_api.repository.VoteRepository;
import br.com.marlon.voting_api.repository.VotingSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class VotingSessionService {

    private final VotingSessionRepository votingSessionRepository;
    private final AgendaItemRepository agendaItemRepository;
    private final VoteRepository voteRepository;

    public VotingSessionService(
            VotingSessionRepository votingSessionRepository,
            AgendaItemRepository agendaItemRepository,
            VoteRepository voteRepository
    ){
        this.votingSessionRepository = votingSessionRepository;
        this.agendaItemRepository = agendaItemRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional
    public VotingSession open(OpenVotingSessionRequest request) {
        AgendaItem agendaItem = agendaItemRepository.findById(request.agendaItemId())
                .orElseThrow(() -> new EntityNotFoundException("Agenda item not found"));

        if (votingSessionRepository.existsByAgendaItemId(request.agendaItemId())) {
            throw new IllegalStateException(
                    "A voting session already exists for this agenda item"
            );
        }

        int durationMinutes = request.durationMinutes() == null
                ? 1
                : request.durationMinutes();

        OffsetDateTime openedAt = OffsetDateTime.now();

        VotingSession votingSession = new VotingSession();
        votingSession.setAgendaItem(agendaItem);
        votingSession.setOpenedAt(openedAt);
        votingSession.setClosesAt(openedAt.plusMinutes(durationMinutes));

        return votingSessionRepository.save(votingSession);
    }

    public VotingResultResponse getResult(Long votingSessionId) {
        VotingSession votingSession = votingSessionRepository
                .findById(votingSessionId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Voting session not found")
                );

        if (OffsetDateTime.now().isBefore(votingSession.getClosesAt())) {
            throw new IllegalStateException("Voting session is still open");
        }

        long yesVotes = voteRepository.countByVotingSessionIdAndChoice(
                votingSessionId,
                VoteChoice.YES
        );

        long noVotes = voteRepository.countByVotingSessionIdAndChoice(
                votingSessionId,
                VoteChoice.NO
        );

        long totalVotes = yesVotes + noVotes;

        String result;

        if (yesVotes > noVotes) {
            result = "APPROVED";
        } else if ( noVotes > yesVotes) {
            result = "REJECTED";
        } else {
            result = "TIED";
        }

        return new VotingResultResponse(
                votingSession.getId(),
                votingSession.getAgendaItem().getId(),
                votingSession.getAgendaItem().getTitle(),
                yesVotes,
                noVotes,
                totalVotes,
                result
        );
    }

}
