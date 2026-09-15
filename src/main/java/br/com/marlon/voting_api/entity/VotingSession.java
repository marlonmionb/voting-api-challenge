package br.com.marlon.voting_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Entity
@Table(name = "voting_sessions", schema = "public")
public class VotingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agenda_item_id", nullable = false, unique = true)
    private AgendaItem agendaItem;

    @NotNull
    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime openedAt;

    @NotNull
    @Column(name = "closes_at", nullable = false)
    private OffsetDateTime closesAt;

    @Column(name = "result_published_at")
    private OffsetDateTime resultPublishedAt;

    public OffsetDateTime getResultPublishedAt() {
        return resultPublishedAt;
    }

    public void setResultPublishedAt(OffsetDateTime resultPublishedAt) {
        this.resultPublishedAt = resultPublishedAt;
    }

    @PrePersist
    private void setOpenedAtOnCreate() {
        if (openedAt == null) {
            openedAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AgendaItem getAgendaItem() {
        return agendaItem;
    }

    public void setAgendaItem(AgendaItem agendaItem) {
        this.agendaItem = agendaItem;
    }

    public OffsetDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(OffsetDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public OffsetDateTime getClosesAt() {
        return closesAt;
    }

    public void setClosesAt(OffsetDateTime closesAt) {
        this.closesAt = closesAt;
    }
}
