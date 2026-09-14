package br.com.marlon.voting_api.controller;

import br.com.marlon.voting_api.dto.request.OpenVotingSessionRequest;
import br.com.marlon.voting_api.dto.response.VotingResultResponse;
import br.com.marlon.voting_api.dto.response.VotingSessionResponse;
import br.com.marlon.voting_api.entity.VotingSession;
import br.com.marlon.voting_api.service.VotingSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class VotingSessionController {

    private final VotingSessionService votingSessionService;

    public VotingSessionController(VotingSessionService votingSessionService) {
        this.votingSessionService = votingSessionService;
    }

    @PostMapping("/voting-sessions")
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
    public ResponseEntity<VotingResultResponse> getResult(
            @PathVariable Long votingSessionId
    ) {
        VotingResultResponse response = votingSessionService
                .getResult(votingSessionId);

        return ResponseEntity.ok(response);
    }
}
