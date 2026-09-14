package br.com.marlon.voting_api.controller;

import br.com.marlon.voting_api.dto.request.CreateAgendaItemRequest;
import br.com.marlon.voting_api.dto.response.AgendaItemResponse;
import br.com.marlon.voting_api.dto.screen.FormButtonResponse;
import br.com.marlon.voting_api.dto.screen.FormItemResponse;
import br.com.marlon.voting_api.dto.screen.FormScreenResponse;
import br.com.marlon.voting_api.entity.AgendaItem;
import br.com.marlon.voting_api.service.AgendaItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Agenda items", description = "Agenda item creation and mobile form endpoints.")
public class AgendaItemController {
    private final AgendaItemService agendaItemService;

    public AgendaItemController(AgendaItemService agendaItemService) {
        this.agendaItemService = agendaItemService;
    }

    @GetMapping("/screens/agenda-items/new")
    @Operation(summary = "Get the agenda item creation screen")
    @ApiResponse(responseCode = "200", description = "Mobile form definition returned")
    public FormScreenResponse getCreateForm() {
        return new FormScreenResponse(
                "FORMULÁRIO",
                "Crie uma pauta",
                List.of(
                    new FormItemResponse(
                    "INPUT_TEXTO",
                    "title",
                    "Nome da Pauta",
                    ""
                ),
                    new FormItemResponse(
                    "INPUT_TEXTO",
                    "description",
                    "Descrição da Pauta",
                    ""
                    )
            ),
            new FormButtonResponse(
                    "Create",
                    "http://localhost:8080/api/v1/agenda-items",
                    Map.of()
            ),
            new FormButtonResponse(
                    "Cancel",
                    "http://localhost:8080/api/v1/screens/home",
                    Map.of()
            )
        );
    }


    @PostMapping("/agenda-items")
    @Operation(summary = "Create an agenda item")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agenda item created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<AgendaItemResponse> createAgendaItem(
            @Valid @RequestBody CreateAgendaItemRequest request
    ) {
        AgendaItem agendaItem = agendaItemService.create(request);

        AgendaItemResponse response = new AgendaItemResponse(
                agendaItem.getId(),
                agendaItem.getTitle(),
                agendaItem.getDescription(),
                agendaItem.getCreatedAt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

}
