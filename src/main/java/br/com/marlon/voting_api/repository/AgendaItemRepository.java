package br.com.marlon.voting_api.repository;

import br.com.marlon.voting_api.entity.AgendaItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaItemRepository extends JpaRepository<AgendaItem, Long> {
}
