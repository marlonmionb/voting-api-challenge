package br.com.marlon.voting_api.controller;

import br.com.marlon.voting_api.entity.AgendaItem;
import br.com.marlon.voting_api.exception.GlobalExceptionHandler;
import br.com.marlon.voting_api.service.AgendaItemService;
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
class AgendaItemControllerTest {

    @Mock
    private AgendaItemService agendaItemService;

    private MockMvc mockMvc;
    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AgendaItemController(agendaItemService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    @Test
    void shouldCreateAgendaItem() throws Exception {
        AgendaItem agendaItem = new AgendaItem();
        agendaItem.setId(1L);
        agendaItem.setTitle("Approve annual budget");
        agendaItem.setDescription("Voting on the annual budget proposal");
        agendaItem.setCreatedAt(OffsetDateTime.now());

        when(agendaItemService.create(any())).thenReturn(agendaItem);

        String requestBody = """
                {
                  "title": "Approve annual budget",
                  "description": "Voting on the annual budget proposal"
                }
                """;

        mockMvc.perform(post("/api/v1/agenda-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Approve annual budget"))
                .andExpect(jsonPath("$.description")
                        .value("Voting on the annual budget proposal"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(agendaItemService).create(any());
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        String requestBody = """
                {
                  "title": "",
                  "description": "Voting on the annual budget proposal"
                }
                """;

        mockMvc.perform(post("/api/v1/agenda-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors.title").exists());

        verifyNoInteractions(agendaItemService);
    }
}
