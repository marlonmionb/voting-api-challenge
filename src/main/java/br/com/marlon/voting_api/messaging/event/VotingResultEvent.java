package br.com.marlon.voting_api.messaging.event;

import java.time.OffsetDateTime;

public record VotingResultEvent(
        Long votingSessionId,
        Long agendaItemId,
        String agendaItemTitle,
        long yesVotes,
        long noVotes,
        long totalVotes,
        String result,
        OffsetDateTime closedAt
) {
}
