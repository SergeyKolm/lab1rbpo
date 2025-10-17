package org.example.model;

import lombok.Data;

@Data
public class Standing {
    private Long id;
    private Long teamId;
    private Integer played;
    private Integer wins;
    private Integer draws;
    private Integer losses;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private Integer points;

    public Team getTeam() { return null; } // временно
}