package org.example.repository;

import org.example.model.Team;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TeamRepository {
    private final Map<Long, Team> teams = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public List<Team> findAll() {
        return new ArrayList<>(teams.values());
    }

    public Optional<Team> findById(Long id) {
        return Optional.ofNullable(teams.get(id));
    }

    public Team save(Team team) {
        if (team.getId() == null) {
            team.setId(counter.getAndIncrement());
        }
        teams.put(team.getId(), team);
        return team;
    }

    public void deleteById(Long id) {
        teams.remove(id);
    }
}