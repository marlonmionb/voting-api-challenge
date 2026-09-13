package br.com.marlon.voting_api.controller;

import br.com.marlon.voting_api.dto.request.CastVoteRequest;
import br.com.marlon.voting_api.dto.response.CastVoteResponse;
import br.com.marlon.voting_api.entity.Vote;
import br.com.marlon.voting_api.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/votes")
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
