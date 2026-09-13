package br.com.marlon.voting_api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OpenVotingSessionRequest(
        @NotNull
        Long agendaItemId,

        @Positive
        Integer durationMinutes
) {
}
