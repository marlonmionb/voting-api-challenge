package br.com.marlon.voting_api.service;

import br.com.marlon.voting_api.client.CpfEligibilityClient;
import br.com.marlon.voting_api.client.VoteEligibility;
import br.com.marlon.voting_api.dto.request.CastVoteRequest;
import br.com.marlon.voting_api.entity.Vote;
import br.com.marlon.voting_api.entity.VoteChoice;
import br.com.marlon.voting_api.entity.VotingSession;
import br.com.marlon.voting_api.exception.ExternalServiceUnavailableException;
import br.com.marlon.voting_api.repository.VoteRepository;
import br.com.marlon.voting_api.repository.VotingSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private VotingSessionRepository votingSessionRepository;

    @Mock
    private CpfEligibilityClient cpfEligibilityClient;

    private VoteService voteService;

    @BeforeEach
    void setUp() {
        voteService = new VoteService(
                voteRepository,
                votingSessionRepository,
                cpfEligibilityClient
        );
    }

    @Test
    void shouldThrowWhenVotingSessionDoesNotExist() {
        CastVoteRequest request = createRequest();

        when(votingSessionRepository.findById(request.votingSessionId()))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> voteService.cast(request)
        );

        verifyNoInteractions(voteRepository);
    }

    @Test
    void shouldRejectVoteWhenVotingSessionIsClosed() {
        CastVoteRequest request = createRequest();
        VotingSession closedSession = createClosedSession();

        when(votingSessionRepository.findById(request.votingSessionId()))
                .thenReturn(Optional.of(closedSession));

        assertThrows(
                IllegalStateException.class,
                () -> voteService.cast(request)
        );

        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    void shouldRejectVoteWhenVotingSessionHasNotOpenedYet() {
        CastVoteRequest request = createRequest();
        VotingSession futureSession = createFutureSession();

        when(votingSessionRepository.findById(request.votingSessionId()))
                .thenReturn(Optional.of(futureSession));

        assertThrows(
                IllegalStateException.class,
                () -> voteService.cast(request)
        );

        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    void shouldRejectVoteWhenAssociateAlreadyVoted() {
        CastVoteRequest request = createRequest();
        VotingSession openSession = createOpenSession();

        when(votingSessionRepository.findById(request.votingSessionId()))
                .thenReturn(Optional.of(openSession));

        when(voteRepository.existsByVotingSessionIdAndAssociateCpf(
                openSession.getId(),
                "62094079007"
        )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> voteService.cast(request)
        );

        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    void shouldNotSaveVoteWhenCpfEligibilityServiceIsUnavailable() {
        CastVoteRequest request = createRequest();
        VotingSession openSession = createOpenSession();

        when(votingSessionRepository.findById(request.votingSessionId()))
                .thenReturn(Optional.of(openSession));
        when(voteRepository.existsByVotingSessionIdAndAssociateCpf(
                openSession.getId(),
                "62094079007"
        )).thenReturn(false);
        when(cpfEligibilityClient.checkEligibility("62094079007"))
                .thenThrow(new ExternalServiceUnavailableException(
                        "CPF eligibility service returned an invalid response",
                        null
                ));

        assertThrows(
                ExternalServiceUnavailableException.class,
                () -> voteService.cast(request)
        );

        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    void shouldSaveVoteWhenSessionIsOpenAndAssociateHasNotVoted() {
        CastVoteRequest request = createRequest();
        VotingSession openSession = createOpenSession();

        when(votingSessionRepository.findById(request.votingSessionId()))
                .thenReturn(Optional.of(openSession));

        when(voteRepository.existsByVotingSessionIdAndAssociateCpf(
                openSession.getId(),
                "62094079007"
        )).thenReturn(false);

        when(cpfEligibilityClient.checkEligibility("62094079007"))
                .thenReturn(VoteEligibility.ABLE_TO_VOTE);

        when(voteRepository.save(any(Vote.class)))
                .thenAnswer(invocation -> {
                    Vote vote = invocation.getArgument(0);
                    vote.setId(10L);
                    return vote;
                });

        Vote savedVote = voteService.cast(request);

        ArgumentCaptor<Vote> voteCaptor =
                ArgumentCaptor.forClass(Vote.class);

        verify(voteRepository).save(voteCaptor.capture());

        Vote capturedVote = voteCaptor.getValue();

        assertEquals(10L, savedVote.getId());
        assertEquals(openSession, capturedVote.getVotingSession());
        assertEquals("62094079007", capturedVote.getAssociateCpf());
        assertEquals(VoteChoice.YES, capturedVote.getChoice());
        assertTrue(capturedVote.getVotedAt() == null);
    }

    private CastVoteRequest createRequest() {
        return new CastVoteRequest(
                1L,
                "620.940.790-07",
                VoteChoice.YES
        );
    }

    private VotingSession createOpenSession() {
        VotingSession votingSession = new VotingSession();
        votingSession.setId(1L);
        votingSession.setOpenedAt(OffsetDateTime.now().minusHours(1));
        votingSession.setClosesAt(OffsetDateTime.now().plusHours(1));

        return votingSession;
    }

    private VotingSession createClosedSession() {
        VotingSession votingSession = new VotingSession();
        votingSession.setId(1L);
        votingSession.setOpenedAt(OffsetDateTime.now().minusHours(2));
        votingSession.setClosesAt(OffsetDateTime.now().minusHours(1));

        return votingSession;
    }

    private VotingSession createFutureSession() {
        VotingSession votingSession = new VotingSession();
        votingSession.setId(1L);
        votingSession.setOpenedAt(OffsetDateTime.now().plusHours(1));
        votingSession.setClosesAt(OffsetDateTime.now().plusHours(2));

        return votingSession;
    }
}
