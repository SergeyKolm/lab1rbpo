package org.example.repository;

import org.example.model.Standing;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class StandingRepository {
    private final Map<Long, Standing> standings = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public List<Standing> findAll() {
        return new ArrayList<>(standings.values());
    }

    public Optional<Standing> findById(Long id) {
        return Optional.ofNullable(standings.get(id));
    }

    public Optional<Standing> findByTeamId(Long teamId) {
        return standings.values().stream()
                .filter(standing -> standing.getTeamId().equals(teamId))
                .findFirst();
    }

    public Standing save(Standing standing) {
        if (standing.getId() == null) {
            standing.setId(counter.getAndIncrement());
        }
        standings.put(standing.getId(), standing);
        return standing;
    }

    public void deleteById(Long id) {
        standings.remove(id);
    }
}