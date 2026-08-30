package com.rotacerta.api.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "drone_mission_events")
public class DroneMissionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private DroneMission mission;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_status", length = 30)
    private DroneMissionStatus missionStatus;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 160)
    private String actor;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected DroneMissionEvent() {}

    public DroneMissionEvent(
            DroneMission mission,
            String eventType,
            DroneMissionStatus missionStatus,
            String title,
            String description,
            String actor,
            OffsetDateTime createdAt
    ) {
        this.mission = mission;
        this.eventType = eventType;
        this.missionStatus = missionStatus;
        this.title = title;
        this.description = description;
        this.actor = actor;
        this.createdAt = createdAt == null ? OffsetDateTime.now() : createdAt;
    }

    public Long getId() { return id; }
    public DroneMission getMission() { return mission; }
    public String getEventType() { return eventType; }
    public DroneMissionStatus getMissionStatus() { return missionStatus; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getActor() { return actor; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
