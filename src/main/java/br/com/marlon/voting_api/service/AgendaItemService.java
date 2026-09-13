package br.com.marlon.voting_api.service;

import br.com.marlon.voting_api.dto.request.CreateAgendaItemRequest;
import br.com.marlon.voting_api.entity.AgendaItem;
import br.com.marlon.voting_api.repository.AgendaItemRepository;
import org.springframework.stereotype.Service;

@Service
public class AgendaItemService {

    // receber titulo e descricao
    // criar AgendaItem
    // salvar com agendaItemRepository
    // devolver a pauta criada

    private AgendaItemRepository agendaItemRepository;

    public AgendaItemService(AgendaItemRepository agendaItemRepository) {
        this.agendaItemRepository = agendaItemRepository;
    }

    public AgendaItem create(CreateAgendaItemRequest request) {
        AgendaItem agendaItem = new AgendaItem();

        agendaItem.setTitle(request.title());
        agendaItem.setDescription(request.description());

        return agendaItemRepository.save(agendaItem);
    }
}
