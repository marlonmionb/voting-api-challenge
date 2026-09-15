package br.com.marlon.voting_api.messaging.scheduler;

import br.com.marlon.voting_api.dto.response.VotingResultResponse;
import br.com.marlon.voting_api.entity.AgendaItem;
import br.com.marlon.voting_api.entity.VotingSession;
import br.com.marlon.voting_api.messaging.event.VotingResultEvent;
import br.com.marlon.voting_api.messaging.publisher.VotingResultPublisher;
import br.com.marlon.voting_api.repository.VotingSessionRepository;
import br.com.marlon.voting_api.service.VotingSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotingResultPublicationSchedulerTest {

    @Mock
    private VotingSessionRepository votingSessionRepository;

    @Mock
    private VotingSessionService votingSessionService;

    @Mock
    private VotingResultPublisher votingResultPublisher;

    private VotingResultPublicationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new VotingResultPublicationScheduler(
                votingSessionRepository,
                votingSessionService,
                votingResultPublisher
        );
    }

    @Test
    void shouldPublishAndMarkClosedVotingSession() {
        VotingSession votingSession = createClosedVotingSession();
        VotingResultResponse result = new VotingResultResponse(
                1L,
                2L,
                "Approve annual budget",
                3,
                1,
                4,
                "APPROVED"
        );

        when(votingSessionRepository
                .findByClosesAtLessThanEqualAndResultPublishedAtIsNull(
                        any(OffsetDateTime.class)
                ))
                .thenReturn(List.of(votingSession));
        when(votingSessionService.getResult(1L)).thenReturn(result);

        scheduler.publishClosedVotingResults();

        ArgumentCaptor<VotingResultEvent> eventCaptor =
                ArgumentCaptor.forClass(VotingResultEvent.class);

        verify(votingResultPublisher).publish(eventCaptor.capture());

        VotingResultEvent publishedEvent = eventCaptor.getValue();

        assertEquals(1L, publishedEvent.votingSessionId());
        assertEquals(2L, publishedEvent.agendaItemId());
        assertEquals("APPROVED", publishedEvent.result());
        assertEquals(votingSession.getClosesAt(), publishedEvent.closedAt());
        assertNotNull(votingSession.getResultPublishedAt());
    }

    @Test
    void shouldNotPublishWhenThereAreNoPendingVotingSessions() {
        when(votingSessionRepository
                .findByClosesAtLessThanEqualAndResultPublishedAtIsNull(
                        any(OffsetDateTime.class)
                ))
                .thenReturn(List.of());

        scheduler.publishClosedVotingResults();

        verifyNoInteractions(votingSessionService, votingResultPublisher);
    }

    private VotingSession createClosedVotingSession() {
        AgendaItem agendaItem = new AgendaItem();
        agendaItem.setId(2L);
        agendaItem.setTitle("Approve annual budget");

        VotingSession votingSession = new VotingSession();
        votingSession.setId(1L);
        votingSession.setAgendaItem(agendaItem);
        votingSession.setOpenedAt(OffsetDateTime.now().minusMinutes(2));
        votingSession.setClosesAt(OffsetDateTime.now().minusMinutes(1));

        return votingSession;
    }
}
