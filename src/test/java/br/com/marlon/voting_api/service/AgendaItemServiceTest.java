package br.com.marlon.voting_api.service;

import br.com.marlon.voting_api.dto.request.CreateAgendaItemRequest;
import br.com.marlon.voting_api.entity.AgendaItem;
import br.com.marlon.voting_api.repository.AgendaItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaItemServiceTest {

    @Mock
    private AgendaItemRepository agendaItemRepository;

    private AgendaItemService agendaItemService;

    @BeforeEach
    void setUp() {
        agendaItemService = new AgendaItemService(agendaItemRepository);
    }

    @Test
    void shouldCreateAgendaItemFromRequest() {
        CreateAgendaItemRequest request = new CreateAgendaItemRequest(
                "Approve annual budget",
                "Voting on the annual budget proposal"
        );

        when(agendaItemRepository.save(any(AgendaItem.class)))
                .thenAnswer(invocation -> {
                    AgendaItem agendaItem = invocation.getArgument(0);
                    agendaItem.setId(1L);
                    return agendaItem;
                });

        AgendaItem savedAgendaItem = agendaItemService.create(request);

        ArgumentCaptor<AgendaItem> agendaItemCaptor =
                ArgumentCaptor.forClass(AgendaItem.class);

        verify(agendaItemRepository).save(agendaItemCaptor.capture());

        AgendaItem capturedAgendaItem = agendaItemCaptor.getValue();

        assertEquals(1L, savedAgendaItem.getId());
        assertEquals(request.title(), capturedAgendaItem.getTitle());
        assertEquals(
                request.description(),
                capturedAgendaItem.getDescription()
        );
    }
}
