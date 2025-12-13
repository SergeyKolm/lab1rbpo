package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "city")
    private String city;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "field_type")
    private String fieldType; // GRASS, ARTIFICIAL_TURF
}