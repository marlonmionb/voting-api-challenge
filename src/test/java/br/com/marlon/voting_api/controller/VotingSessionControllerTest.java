package br.com.marlon.voting_api.controller;

import br.com.marlon.voting_api.dto.response.VotingResultResponse;
import br.com.marlon.voting_api.entity.AgendaItem;
import br.com.marlon.voting_api.entity.VotingSession;
import br.com.marlon.voting_api.exception.GlobalExceptionHandler;
import br.com.marlon.voting_api.service.VotingSessionService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VotingSessionControllerTest {

    @Mock
    private VotingSessionService votingSessionService;

    private MockMvc mockMvc;
    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new VotingSessionController(votingSessionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    @Test
    void shouldOpenVotingSession() throws Exception {
        AgendaItem agendaItem = new AgendaItem();
        agendaItem.setId(1L);

        VotingSession votingSession = new VotingSession();
        votingSession.setId(10L);
        votingSession.setAgendaItem(agendaItem);
        votingSession.setOpenedAt(OffsetDateTime.now());
        votingSession.setClosesAt(OffsetDateTime.now().plusMinutes(1));

        when(votingSessionService.open(any())).thenReturn(votingSession);

        String requestBody = """
                {
                  "agendaItemId": 1,
                  "durationMinutes": 1
                }
                """;

        mockMvc.perform(post("/api/v1/voting-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.agendaItemId").value(1))
                .andExpect(jsonPath("$.openedAt").exists())
                .andExpect(jsonPath("$.closesAt").exists());

        verify(votingSessionService).open(any());
    }

    @Test
    void shouldReturnBadRequestWhenDurationIsNotPositive() throws Exception {
        String requestBody = """
                {
                  "agendaItemId": 1,
                  "durationMinutes": 0
                }
                """;

        mockMvc.perform(post("/api/v1/voting-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors.durationMinutes").exists());

        verifyNoInteractions(votingSessionService);
    }

    @Test
    void shouldReturnVotingResult() throws Exception {
        VotingResultResponse response = new VotingResultResponse(
                10L,
                1L,
                "Approve annual budget",
                3,
                1,
                4,
                "APPROVED"
        );

        when(votingSessionService.getResult(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/voting-sessions/10/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.votingSessionId").value(10))
                .andExpect(jsonPath("$.yesVotes").value(3))
                .andExpect(jsonPath("$.noVotes").value(1))
                .andExpect(jsonPath("$.totalVotes").value(4))
                .andExpect(jsonPath("$.result").value("APPROVED"));
    }

    @Test
    void shouldReturnConflictWhenResultIsRequestedForOpenSession() throws Exception {
        when(votingSessionService.getResult(10L))
                .thenThrow(new IllegalStateException("Voting session is still open"));

        mockMvc.perform(get("/api/v1/voting-sessions/10/result"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail")
                        .value("Voting session is still open"));
    }
}
