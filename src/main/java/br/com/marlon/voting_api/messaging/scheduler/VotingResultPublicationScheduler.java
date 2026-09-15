package br.com.marlon.voting_api.messaging.scheduler;

import br.com.marlon.voting_api.dto.response.VotingResultResponse;
import br.com.marlon.voting_api.entity.VotingSession;
import br.com.marlon.voting_api.messaging.event.VotingResultEvent;
import br.com.marlon.voting_api.messaging.publisher.VotingResultPublisher;
import br.com.marlon.voting_api.repository.VotingSessionRepository;
import br.com.marlon.voting_api.service.VotingSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component

public class VotingResultPublicationScheduler {


    private static final Logger log =
            LoggerFactory.getLogger(VotingResultPublicationScheduler.class);

    private final VotingSessionRepository votingSessionRepository;
    private final VotingSessionService votingSessionService;
    private final VotingResultPublisher votingResultPublisher;

    public VotingResultPublicationScheduler(
            VotingSessionRepository votingSessionRepository,
            VotingSessionService votingSessionService,
            VotingResultPublisher votingResultPublisher
    ) {
        this.votingSessionRepository = votingSessionRepository;
        this.votingSessionService = votingSessionService;
        this.votingResultPublisher = votingResultPublisher;
    }

    @Scheduled(
            fixedDelayString = "${app.messaging.voting-result.fixed-delay:10s}"
    )
    @Transactional
    public void publishClosedVotingResults() {
        List<VotingSession> closedSessions =
                votingSessionRepository
                        .findByClosesAtLessThanEqualAndResultPublishedAtIsNull(
                                OffsetDateTime.now()
                        );
        for (VotingSession votingSession : closedSessions) {
            VotingResultResponse result = votingSessionService
                    .getResult(votingSession.getId());

            VotingResultEvent event = new VotingResultEvent(
                    result.votingSessionId(),
                    result.agendaItemId(),
                    result.agendaItemTitle(),
                    result.yesVotes(),
                    result.noVotes(),
                    result.totalVotes(),
                    result.result(),
                    votingSession.getClosesAt()
            );

            votingResultPublisher.publish(event);

            votingSession.setResultPublishedAt(OffsetDateTime.now());

            log.info(
                    "Voting result published. votingSessionId={}, result={}",
                    votingSession.getId(),
                    result.result()
            );
        }
    }
}
