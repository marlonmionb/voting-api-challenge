package br.com.marlon.voting_api.dto.response;

public record VotingResultResponse(
    Long votingSessionId,
    Long agendaItemId,
    String agendaItemTitle,
    long yesVotes,
    long noVotes,
    long totalVotes,
    String result
) {
}
