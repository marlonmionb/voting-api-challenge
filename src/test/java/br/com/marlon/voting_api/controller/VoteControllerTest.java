package br.com.marlon.voting_api.controller;

import br.com.marlon.voting_api.entity.Vote;
import br.com.marlon.voting_api.entity.VoteChoice;
import br.com.marlon.voting_api.entity.VotingSession;
import br.com.marlon.voting_api.exception.GlobalExceptionHandler;
import br.com.marlon.voting_api.service.VoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VoteControllerTest {

    @Mock
    private VoteService voteService;

    private MockMvc mockMvc;
    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new VoteController(voteService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    @Test
    void shouldCastVote() throws Exception {
        VotingSession votingSession = new VotingSession();
        votingSession.setId(1L);

        Vote vote = new Vote();
        vote.setId(10L);
        vote.setVotingSession(votingSession);
        vote.setChoice(VoteChoice.YES);
        vote.setVotedAt(OffsetDateTime.now());

        when(voteService.cast(any())).thenReturn(vote);

        String requestBody = """
                {
                  "votingSessionId": 1,
                  "associateCpf": "62094079007",
                  "choice": "YES"
                }
                """;

        mockMvc.perform(post("/api/v1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.votingSessionId").value(1))
                .andExpect(jsonPath("$.choice").value("YES"))
                .andExpect(jsonPath("$.votedAt").exists());

        verify(voteService).cast(any());
    }

    @Test
    void shouldReturnBadRequestWhenCpfIsInvalid() throws Exception {
        String requestBody = """
                {
                  "votingSessionId": 1,
                  "associateCpf": "62094079006",
                  "choice": "YES"
                }
                """;

        mockMvc.perform(post("/api/v1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors.associateCpf").exists());

        verifyNoInteractions(voteService);
    }

    @Test
    void shouldReturnConflictWhenVoteServiceRejectsVote() throws Exception {
        when(voteService.cast(any()))
                .thenThrow(new IllegalStateException("Voting session is not open"));

        String requestBody = """
                {
                  "votingSessionId": 1,
                  "associateCpf": "62094079007",
                  "choice": "YES"
                }
                """;

        mockMvc.perform(post("/api/v1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail")
                        .value("Voting session is not open"));
    }
}
