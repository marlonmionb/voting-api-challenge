package br.com.marlon.voting_api.dto.screen;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FormItemResponse(
        @JsonProperty("tipo") String type,
        @JsonProperty("id") String id,
        @JsonProperty("titulo") String title,
        @JsonProperty("valor") Object value
) {
}