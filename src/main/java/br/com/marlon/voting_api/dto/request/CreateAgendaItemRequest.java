package br.com.marlon.voting_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAgendaItemRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        String description
) {
}