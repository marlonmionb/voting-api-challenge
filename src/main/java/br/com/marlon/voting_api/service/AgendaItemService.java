package br.com.marlon.voting_api.service;

import br.com.marlon.voting_api.dto.request.CreateAgendaItemRequest;
import br.com.marlon.voting_api.entity.AgendaItem;
import br.com.marlon.voting_api.repository.AgendaItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgendaItemService {

    private static final Logger log = LoggerFactory.getLogger(AgendaItemService.class);


    private final AgendaItemRepository agendaItemRepository;

    public AgendaItemService(AgendaItemRepository agendaItemRepository) {
        this.agendaItemRepository = agendaItemRepository;
    }

    public AgendaItem create(CreateAgendaItemRequest request) {
        AgendaItem agendaItem = new AgendaItem();

        agendaItem.setTitle(request.title());
        agendaItem.setDescription(request.description());

        AgendaItem savedAgendaItem = agendaItemRepository.save(agendaItem);

        log.info(
                "Agenda item created. agendaItemId={}",
                savedAgendaItem.getId()
        );

        return savedAgendaItem;
    }
}
