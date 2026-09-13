package br.com.marlon.voting_api.dto.request;


import br.com.marlon.voting_api.entity.VoteChoice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

public record CastVoteRequest(
        @NotNull
        Long votingSessionId,

        @NotBlank
        @CPF
        String associateCpf,

        @NotNull
        VoteChoice choice
) {
}
