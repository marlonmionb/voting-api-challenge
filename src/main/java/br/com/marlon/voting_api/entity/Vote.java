package br.com.marlon.voting_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Entity
@Table(name = "votes", schema = "public")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voting_session_id", nullable = false)
    private VotingSession votingSession;

    @NotNull
    @Column(name = "associate_cpf", nullable = false)
    private String associateCpf;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "choice", nullable = false, length = 3)
    private VoteChoice choice;

    @NotNull
    @Column(name = "voted_at", nullable = false)
    private OffsetDateTime votedAt;

    @PrePersist
    private void setVotedAtOnCreate() {
        if (votedAt == null) {
            votedAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VotingSession getVotingSession() {
        return votingSession;
    }

    public void setVotingSession(VotingSession votingSession) {
        this.votingSession = votingSession;
    }

    public String getAssociateCpf() {
        return associateCpf;
    }

    public void setAssociateCpf(String associateCpf) {
        this.associateCpf = associateCpf;
    }

    public VoteChoice getChoice() {
        return choice;
    }

    public void setChoice(VoteChoice choice) {
        this.choice = choice;
    }

    public OffsetDateTime getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(OffsetDateTime votedAt) {
        this.votedAt = votedAt;
    }


}
