ALTER TABLE votes
    RENAME COLUMN associate_id TO associate_cpf;

ALTER TABLE votes
    RENAME CONSTRAINT uq_votes_session_associate
    TO uq_votes_session_associate_cpf;