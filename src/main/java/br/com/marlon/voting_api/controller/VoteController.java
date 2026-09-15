package br.com.marlon.voting_api.controller;

import br.com.marlon.voting_api.dto.request.CastVoteRequest;
import br.com.marlon.voting_api.dto.response.CastVoteResponse;
import br.com.marlon.voting_api.entity.Vote;
import br.com.marlon.voting_api.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Votes", description = "Vote registration endpoints.")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/votes")
    @Operation(summary = "Cast a vote in an open voting session")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vote recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Voting session or CPF not found"),
            @ApiResponse(responseCode = "409", description = "Voting session is closed, associate already voted, or associate is not eligible"),
            @ApiResponse(responseCode = "503", description = "CPF eligibility service unavailable")
    })
    public ResponseEntity<CastVoteResponse> cast(
            @Valid @RequestBody CastVoteRequest request
    ) {

        Vote vote = voteService.cast(request);

        CastVoteResponse response = new CastVoteResponse(
                vote.getId(),
                vote.getVotingSession().getId(),
                vote.getChoice(),
                vote.getVotedAt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


}
