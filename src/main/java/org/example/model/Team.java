package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "city")
    private String city;

    @Column(name = "coach_name")
    private String coachName;

    @Column(name = "foundation_year")
    private Integer foundationYear;

    @Column(nullable = false)
    private Integer points = 0;
}