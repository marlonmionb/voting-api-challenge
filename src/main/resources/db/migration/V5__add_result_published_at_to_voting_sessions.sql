ALTER TABLE voting_sessions
    ADD COLUMN result_published_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_voting_sessions_peding_result_publication
ON voting_sessions (closes_at)
WHERE result_published_at IS NULL;