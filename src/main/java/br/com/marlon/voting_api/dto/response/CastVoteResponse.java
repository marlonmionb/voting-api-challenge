package br.com.marlon.voting_api.dto.response;

import br.com.marlon.voting_api.entity.VoteChoice;

import java.time.OffsetDateTime;

public record CastVoteResponse(
        Long id,
        Long votingSessionId,
        VoteChoice choice,
        OffsetDateTime votedAt
) {
}
