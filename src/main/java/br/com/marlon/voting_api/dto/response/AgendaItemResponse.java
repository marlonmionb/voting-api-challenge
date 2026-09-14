package br.com.marlon.voting_api.dto.response;

import java.time.OffsetDateTime;

public record AgendaItemResponse(
        Long id,
        String title,
        String description,
        OffsetDateTime createdAt
) {
}