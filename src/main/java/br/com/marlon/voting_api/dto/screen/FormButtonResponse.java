package br.com.marlon.voting_api.dto.screen;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record FormButtonResponse(
        @JsonProperty("texto") String text,
        @JsonProperty("url") String url,
        @JsonProperty("body") Map<String, Object> body
) {
}