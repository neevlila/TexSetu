package com.texsetu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_events")
public class ProductionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private ProductionLot productionLot;

    @NotBlank
    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @Column(name = "previous_state", length = 50)
    private String previousState;

    @NotBlank
    @Column(name = "new_state", nullable = false, length = 50)
    private String newState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @NotBlank
    @Column(name = "actor_role", nullable = false, length = 30)
    private String actorRole;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "event_timestamp", nullable = false, updatable = false)
    private LocalDateTime eventTimestamp = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.eventTimestamp == null) {
            this.eventTimestamp = LocalDateTime.now();
        }
    }

    public ProductionEvent() {}

    public ProductionEvent(ProductionLot productionLot, String eventType, String previousState, String newState, User actor, String actorRole, String description, String attachmentUrl) {
        this.productionLot = productionLot;
        this.eventType = eventType;
        this.previousState = previousState;
        this.newState = newState;
        this.actor = actor;
        this.actorRole = actorRole;
        this.description = description;
        this.attachmentUrl = attachmentUrl;
        this.eventTimestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProductionLot getProductionLot() { return productionLot; }
    public void setProductionLot(ProductionLot productionLot) { this.productionLot = productionLot; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPreviousState() { return previousState; }
    public void setPreviousState(String previousState) { this.previousState = previousState; }

    public String getNewState() { return newState; }
    public void setNewState(String newState) { this.newState = newState; }

    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }
}
