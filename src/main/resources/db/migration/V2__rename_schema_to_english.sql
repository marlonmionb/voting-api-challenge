ALTER TABLE pautas RENAME TO agenda_items;
ALTER TABLE agenda_items RENAME COLUMN titulo TO title;
ALTER TABLE agenda_items RENAME COLUMN descricao TO description;
ALTER TABLE agenda_items RENAME COLUMN criada_em TO created_at;

ALTER TABLE sessoes RENAME TO voting_sessions;
ALTER TABLE voting_sessions RENAME COLUMN pauta_id TO agenda_item_id;
ALTER TABLE voting_sessions RENAME COLUMN aberta_em TO opened_at;
ALTER TABLE voting_sessions RENAME COLUMN encerra_em TO closes_at;

ALTER TABLE voting_sessions
    RENAME CONSTRAINT fk_sessoes_pauta TO fk_voting_sessions_agenda_items;

ALTER TABLE voting_sessions
    RENAME CONSTRAINT uq_sessoes_pauta TO uq_voting_sessions_agenda_item;

ALTER TABLE voting_sessions
    RENAME CONSTRAINT ck_sessoes_periodo TO ck_voting_sessions_period;

ALTER TABLE votos RENAME TO votes;
ALTER TABLE votes RENAME COLUMN sessao_id TO voting_session_id;
ALTER TABLE votes RENAME COLUMN associado_id TO associate_id;
ALTER TABLE votes RENAME COLUMN opcao TO choice;
ALTER TABLE votes RENAME COLUMN votado_em TO voted_at;

ALTER TABLE votes
    RENAME CONSTRAINT fk_votos_sessao TO fk_votes_voting_sessions;

ALTER TABLE votes
    RENAME CONSTRAINT uq_votos_sessao_associado TO uq_votes_session_associate;

ALTER TABLE votes
    RENAME CONSTRAINT ck_votos_opcao TO ck_votes_choice;

ALTER TABLE votes
DROP CONSTRAINT ck_votes_choice;

ALTER TABLE votes
    ADD CONSTRAINT ck_votes_choice
        CHECK (choice IN ('YES', 'NO'));

ALTER INDEX idx_votos_sessao_id
    RENAME TO idx_votes_voting_session_id;