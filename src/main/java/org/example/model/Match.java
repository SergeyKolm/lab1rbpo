package org.example.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Match {
    private Long id;
    private Long homeTeamId;
    private Long awayTeamId;
    private Long venueId;
    private LocalDateTime matchDate;
    private Integer homeTeamScore;
    private Integer awayTeamScore;
    private String status; // SCHEDULED, IN_PROGRESS, FINISHED

    // Геттеры для удобства (если нужны)
    public Team getHomeTeam() { return null; } // временно
    public Team getAwayTeam() { return null; } // временно
    public Venue getVenue() { return null; } // временно
}