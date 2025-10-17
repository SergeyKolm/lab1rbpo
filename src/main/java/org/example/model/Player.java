package org.example.model;

import lombok.Data;

@Data
public class Player {
    private Long id;
    private String name;
    private Integer age;
    private String position;
    private Long teamId; // временно используем ID вместо связи
}