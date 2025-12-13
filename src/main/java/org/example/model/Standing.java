package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "standings")
public class Standing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false, unique = true)
    private Long teamId;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private Integer matchesPlayed = 0;

    @Column(nullable = false)
    private Integer wins = 0;

    @Column(nullable = false)
    private Integer draws = 0;

    @Column(nullable = false)
    private Integer losses = 0;

    @Column(name = "goals_for", nullable = false)
    private Integer goalsFor = 0;

    @Column(name = "goals_against", nullable = false)
    private Integer goalsAgainst = 0;

    @Column(name = "goal_difference")
    private Integer goalDifference = 0;

    @Column(nullable = false)
    private Integer points = 0;
}