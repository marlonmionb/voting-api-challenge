package br.com.marlon.voting_api.dto.screen;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FormScreenResponse(
        @JsonProperty("tipo") String type,
        @JsonProperty("titulo") String title,
        @JsonProperty("itens") List<FormItemResponse> items,
        @JsonProperty("botaoOk") FormButtonResponse confirmButton,
        @JsonProperty("botaoCancelar") FormButtonResponse cancelButton
) {
}
