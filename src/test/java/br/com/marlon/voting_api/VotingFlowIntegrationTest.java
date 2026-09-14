package br.com.marlon.voting_api;

import br.com.marlon.voting_api.dto.request.CastVoteRequest;
import br.com.marlon.voting_api.dto.request.CreateAgendaItemRequest;
import br.com.marlon.voting_api.dto.request.OpenVotingSessionRequest;
import br.com.marlon.voting_api.entity.AgendaItem;
import br.com.marlon.voting_api.entity.Vote;
import br.com.marlon.voting_api.entity.VoteChoice;
import br.com.marlon.voting_api.entity.VotingSession;
import br.com.marlon.voting_api.repository.VoteRepository;
import br.com.marlon.voting_api.service.AgendaItemService;
import br.com.marlon.voting_api.service.VoteService;
import br.com.marlon.voting_api.service.VotingSessionService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class VotingFlowIntegrationTest {

    @Autowired
    private AgendaItemService agendaItemService;

    @Autowired
    private VotingSessionService votingSessionService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistVoteForAnOpenVotingSession() {
        AgendaItem agendaItem = agendaItemService.create(
                new CreateAgendaItemRequest(
                        "Approve annual budget",
                        "Voting on the annual budget proposal"
                )
        );

        VotingSession votingSession = votingSessionService.open(
                new OpenVotingSessionRequest(agendaItem.getId(), 5)
        );

        Vote savedVote = voteService.cast(
                new CastVoteRequest(
                        votingSession.getId(),
                        "620.940.790-07",
                        VoteChoice.YES
                )
        );

        voteRepository.flush();
        entityManager.clear();

        Vote persistedVote = voteRepository.findById(savedVote.getId())
                .orElseThrow();

        assertEquals(votingSession.getId(), persistedVote.getVotingSession().getId());
        assertEquals("62094079007", persistedVote.getAssociateCpf());
        assertEquals(VoteChoice.YES, persistedVote.getChoice());
        assertTrue(persistedVote.getVotedAt() != null);
    }
}
