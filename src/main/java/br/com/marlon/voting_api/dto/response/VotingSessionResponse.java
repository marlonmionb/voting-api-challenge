package br.com.marlon.voting_api.dto.response;

import java.time.OffsetDateTime;

public record VotingSessionResponse(
        Long id,
        Long agendaItemId,
        OffsetDateTime openedAt,
        OffsetDateTime closesAt
) {
}