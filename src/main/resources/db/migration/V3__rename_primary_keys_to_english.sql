ALTER TABLE agenda_items
    RENAME CONSTRAINT pautas_pkey TO pk_agenda_items;

ALTER TABLE voting_sessions
    RENAME CONSTRAINT sessoes_pkey TO pk_voting_sessions;

ALTER TABLE votes
    RENAME CONSTRAINT votos_pkey TO pk_votes;