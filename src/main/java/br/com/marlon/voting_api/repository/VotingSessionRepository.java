package br.com.marlon.voting_api.repository;

import br.com.marlon.voting_api.entity.VotingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface VotingSessionRepository extends JpaRepository<VotingSession, Long> {
    boolean existsByAgendaItemId(Long agendaItemId);

    List<VotingSession> findByClosesAtLessThanEqualAndResultPublishedAtIsNull(
            OffsetDateTime closesAt
    );
}
