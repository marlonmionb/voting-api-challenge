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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotingSessionServiceTest {

    @Mock
    private VotingSessionRepository votingSessionRepository;

    @Mock
    private AgendaItemRepository agendaItemRepository;

    @Mock
    private VoteRepository voteRepository;

    private VotingSessionService votingSessionService;

    @BeforeEach
    void setUp() {
        votingSessionService = new VotingSessionService(
                votingSessionRepository,
                agendaItemRepository,
                voteRepository
        );
    }

    @Test
    void shouldOpenSessionWithDefaultDurationWhenDurationIsNotProvided() {
        AgendaItem agendaItem = createAgendaItem();
        OpenVotingSessionRequest request = new OpenVotingSessionRequest(1L, null);

        when(agendaItemRepository.findById(1L))
                .thenReturn(Optional.of(agendaItem));
        when(votingSessionRepository.existsByAgendaItemId(1L))
                .thenReturn(false);
        when(votingSessionRepository.save(any(VotingSession.class)))
                .thenAnswer(invocation -> {
                    VotingSession session = invocation.getArgument(0);
                    session.setId(10L);
                    return session;
                });

        VotingSession savedSession = votingSessionService.open(request);

        assertEquals(10L, savedSession.getId());
        assertEquals(agendaItem, savedSession.getAgendaItem());
        assertEquals(
                savedSession.getOpenedAt().plusMinutes(1),
                savedSession.getClosesAt()
        );
    }

    @Test
    void shouldOpenSessionWithProvidedDuration() {
        AgendaItem agendaItem = createAgendaItem();
        OpenVotingSessionRequest request = new OpenVotingSessionRequest(1L, 5);

        when(agendaItemRepository.findById(1L))
                .thenReturn(Optional.of(agendaItem));
        when(votingSessionRepository.existsByAgendaItemId(1L))
                .thenReturn(false);
        when(votingSessionRepository.save(any(VotingSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VotingSession savedSession = votingSessionService.open(request);

        assertEquals(
                savedSession.getOpenedAt().plusMinutes(5),
                savedSession.getClosesAt()
        );
    }

    @Test
    void shouldThrowWhenAgendaItemDoesNotExist() {
        OpenVotingSessionRequest request = new OpenVotingSessionRequest(1L, 5);

        when(agendaItemRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> votingSessionService.open(request)
        );

        verifyNoInteractions(votingSessionRepository);
    }

    @Test
    void shouldRejectOpeningSecondSessionForSameAgendaItem() {
        AgendaItem agendaItem = createAgendaItem();
        OpenVotingSessionRequest request = new OpenVotingSessionRequest(1L, 5);

        when(agendaItemRepository.findById(1L))
                .thenReturn(Optional.of(agendaItem));
        when(votingSessionRepository.existsByAgendaItemId(1L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> votingSessionService.open(request)
        );

        verify(votingSessionRepository, never()).save(any(VotingSession.class));
    }

    @Test
    void shouldThrowWhenGettingResultForUnknownSession() {
        when(votingSessionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> votingSessionService.getResult(1L)
        );

        verifyNoInteractions(voteRepository);
    }

    @Test
    void shouldRejectResultWhileVotingSessionIsOpen() {
        VotingSession openSession = createClosedSession();
        openSession.setClosesAt(OffsetDateTime.now().plusMinutes(1));

        when(votingSessionRepository.findById(1L))
                .thenReturn(Optional.of(openSession));

        assertThrows(
                IllegalStateException.class,
                () -> votingSessionService.getResult(1L)
        );

        verifyNoInteractions(voteRepository);
    }

    @Test
    void shouldReturnApprovedResultWhenYesVotesAreGreater() {
        VotingResultResponse response = getResultForVoteCounts(3, 1);

        assertEquals(3, response.yesVotes());
        assertEquals(1, response.noVotes());
        assertEquals(4, response.totalVotes());
        assertEquals("APPROVED", response.result());
    }

    @Test
    void shouldReturnRejectedResultWhenNoVotesAreGreater() {
        VotingResultResponse response = getResultForVoteCounts(1, 3);

        assertEquals(1, response.yesVotes());
        assertEquals(3, response.noVotes());
        assertEquals(4, response.totalVotes());
        assertEquals("REJECTED", response.result());
    }

    @Test
    void shouldReturnTiedResultWhenVoteCountsAreEqual() {
        VotingResultResponse response = getResultForVoteCounts(2, 2);

        assertEquals(2, response.yesVotes());
        assertEquals(2, response.noVotes());
        assertEquals(4, response.totalVotes());
        assertEquals("TIED", response.result());
    }

    private VotingResultResponse getResultForVoteCounts(long yesVotes, long noVotes) {
        VotingSession closedSession = createClosedSession();

        when(votingSessionRepository.findById(1L))
                .thenReturn(Optional.of(closedSession));
        when(voteRepository.countByVotingSessionIdAndChoice(1L, VoteChoice.YES))
                .thenReturn(yesVotes);
        when(voteRepository.countByVotingSessionIdAndChoice(1L, VoteChoice.NO))
                .thenReturn(noVotes);

        return votingSessionService.getResult(1L);
    }

    private AgendaItem createAgendaItem() {
        AgendaItem agendaItem = new AgendaItem();
        agendaItem.setId(1L);
        agendaItem.setTitle("Approve annual budget");

        return agendaItem;
    }

    private VotingSession createClosedSession() {
        VotingSession votingSession = new VotingSession();
        votingSession.setId(1L);
        votingSession.setAgendaItem(createAgendaItem());
        votingSession.setOpenedAt(OffsetDateTime.now().minusHours(2));
        votingSession.setClosesAt(OffsetDateTime.now().minusHours(1));

        return votingSession;
    }
}
