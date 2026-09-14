package br.com.marlon.voting_api.controller;

import br.com.marlon.voting_api.dto.request.OpenVotingSessionRequest;
import br.com.marlon.voting_api.dto.response.VotingResultResponse;
import br.com.marlon.voting_api.dto.response.VotingSessionResponse;
import br.com.marlon.voting_api.entity.VotingSession;
import br.com.marlon.voting_api.service.VotingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Voting sessions", description = "Voting session opening and result endpoints.")
public class VotingSessionController {

    private final VotingSessionService votingSessionService;

    public VotingSessionController(VotingSessionService votingSessionService) {
        this.votingSessionService = votingSessionService;
    }

    @PostMapping("/voting-sessions")
    @Operation(summary = "Open a voting session for an agenda item")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Voting session opened"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Agenda item not found"),
            @ApiResponse(responseCode = "409", description = "Agenda item already has a voting session")
    })
    public ResponseEntity<VotingSessionResponse> open(
            @Valid @RequestBody OpenVotingSessionRequest request
    ) {
        VotingSession votingSession = votingSessionService.open(request);

        VotingSessionResponse response = new VotingSessionResponse(
                votingSession.getId(),
                votingSession.getAgendaItem().getId(),
                votingSession.getOpenedAt(),
                votingSession.getClosesAt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/voting-sessions/{votingSessionId}/result")
    @Operation(summary = "Get the final result of a voting session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voting result returned"),
            @ApiResponse(responseCode = "404", description = "Voting session not found"),
            @ApiResponse(responseCode = "409", description = "Voting session is still open")
    })
    public ResponseEntity<VotingResultResponse> getResult(
            @PathVariable Long votingSessionId
    ) {
        VotingResultResponse response = votingSessionService
                .getResult(votingSessionId);

        return ResponseEntity.ok(response);
    }
}
