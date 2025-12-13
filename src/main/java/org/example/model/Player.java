package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "position")
    private String position; // GOALKEEPER, DEFENDER, MIDFIELDER, FORWARD

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Column(nullable = false)
    private Integer age;

    @Column(name = "goals_scored")
    private Integer goalsScored = 0;
}