package org.example.model;

import lombok.Data;

@Data
public class Venue {
    private Long id;
    private String name;
    private String address;
    private Integer capacity;
}