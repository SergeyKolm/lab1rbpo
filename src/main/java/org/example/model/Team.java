package org.example.model;

import lombok.Data;

import java.util.List;

@Data
public class Team {
    private Long id;
    private String name;
    private String city;
    private String coach;
    private List<Player> players;
}